package qa.gov.mol.utils.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import java.math.BigDecimal;

import java.sql.Connection;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.faces.context.FacesContext;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.sql.DataSource;

import mol.eservices.shared.model.shcprc.vo.ev.RepVwscPeportsTemplatesVoRowImpl;
import mol.eservices.shared.model.shcprc.vo.ev.RepVwscProcessReportsMapVoRowImpl;
import mol.eservices.shared.model.shcprc.vo.ev.RepVwscReportsListVoRowImpl;
import mol.eservices.shared.model.shcprc.vo.ev.RepVwscRequestsRegistryUVOImpl;
import mol.eservices.shared.model.shcprc.vo.ev.RepVwscRequestsRegistryUVORowImpl;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import oracle.adf.share.ADFContext;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Key;
import oracle.jbo.client.Configuration;
import oracle.jbo.server.ApplicationModuleImpl;

@WebServlet(name = "PreviewReport", urlPatterns = { "/previewreport" })

public class PreviewReport extends HttpServlet implements Serializable {
    @SuppressWarnings("compatibility:-7797713830537433159")
    private static final long serialVersionUID = -8285455993411913612L;

    public void init(ServletConfig config) throws ServletException {
        System.setProperty("java.awt.headless", "true");
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            System.out.println("Query String: " + request.getQueryString());
            System.out.println("REGISTRY_ID: " + request.getParameter("REGISTRY_ID"));
            runPdf(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runPdf(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("Query String: " + request.getQueryString());
        System.out.println("REGISTRY_ID: " + request.getParameter("REGISTRY_ID"));
        BigDecimal regestryId = new BigDecimal(request.getParameter("REGISTRY_ID").toString());
        Connection conn = null;
        //Get Registry Record
        String amDef = "mol.eservices.shared.model.shcprc.am.MolShcPrcAppModule";
        String config = "MolShcPrcAppModuleLocal";
        ApplicationModuleImpl amImpl = (ApplicationModuleImpl) Configuration.createRootApplicationModule(amDef, config);
        try {
            RepVwscRequestsRegistryUVOImpl repVwscRequestsRegistryUVO =
                (RepVwscRequestsRegistryUVOImpl) amImpl.findViewObject("RepVwscRequestsRegistryUVO1");
            Object[] rowKey = new Object[1];
            rowKey[0] = regestryId;
            RepVwscRequestsRegistryUVORowImpl row =
                (RepVwscRequestsRegistryUVORowImpl) repVwscRequestsRegistryUVO.findByKey(new Key(rowKey), 1)[0];

            RepVwscProcessReportsMapVoRowImpl mapRow =
                (RepVwscProcessReportsMapVoRowImpl) row.getRepVwscProcessReportsMapVo().first();
            String reportName = mapRow.getReportSource();
            RepVwscPeportsTemplatesVoRowImpl templateRow =
                (RepVwscPeportsTemplatesVoRowImpl) mapRow.getRepVwscPeportsTemplatesVo().first();
            reportName = reportName + templateRow.getLocPath();

            Map paramMap = new HashMap();
            System.out.println("Row: " + row);
            if (row.getRequestParams() != null) {
                int blobLength = (int) row.getRequestParams().getLength();
                byte[] blobAsBytes = row.getRequestParams().getBytes(1, blobLength);
                paramMap = byte2HashMap(blobAsBytes);
            } else {
                paramMap.put("proc_trx_id", row.getProcTrxId());
                paramMap.put("process_id", row.getProcessId());
                paramMap.put("pk_code_1", row.getPkCode1());
                paramMap.put("pk_code_2", row.getPkCode2());
            }


            RepVwscReportsListVoRowImpl reportRow =
                (RepVwscReportsListVoRowImpl) mapRow.getRepVwscReportsListVo().first();

            ServletOutputStream out = response.getOutputStream();
            response.setHeader("Cache-Control", "max-age=0");
            response.setContentType("application/pdf");
            String datasource = "jdbc/mol_shc_prc";
            if (mapRow.getRepId() != null)
                datasource = reportRow.getAppName();
            System.out.println("Data source: " + datasource);
            conn = getConnection(datasource);
            System.out.println("Connection: " + conn);
            java.util.Locale locale = new Locale("ar");
            paramMap.put("REPORT_LOCALE", locale);
            JasperPrint print = JasperFillManager.fillReport(reportName, paramMap, conn);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(print, baos);
            out.write(baos.toByteArray());
            out.flush();
            out.close();
            //            FacesContext.getCurrentInstance().responseComplete();

        } catch (Exception jex) {
            jex.printStackTrace();
        } finally {
            if (amImpl != null)
                releaseApplicationModule(amImpl, null);
            close(conn);

        }
    }

    private boolean releaseApplicationModule(ApplicationModule appModule, ADFContext ctx) {
        try {
            Configuration.releaseRootApplicationModule(appModule, true);
            ADFContext.resetADFContext(ctx);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private Connection getConnection(String datasource) throws Exception {
        if (datasource == null)
            datasource = "jdbc/mol_bm_lwp"; //we need the Datasource
        return getDataSourceConnection(datasource);
    }

    public Connection getDataSourceConnection(String dataSourceName) {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(dataSourceName);
            return ds.getConnection();
        } catch (Exception e) {

        }
        return null;
    }

    public void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private HashMap extractMap(HashMap<String, Object> map) {
        String[] keys = new String[map.size()];
        Object[] values = new Object[map.size()];
        int index = 0;
        HashMap resultMap = new HashMap();

        for (Map.Entry<String, Object> mapEntry : map.entrySet()) {
            resultMap.put(mapEntry.getKey(), mapEntry.getValue());
            index++;
        }
        return resultMap;
    }

    public static HashMap<String, Object> byte2HashMap(byte[] blob) {

        return (HashMap<String, Object>) byte2Object(blob);
    }

    public static Object byte2Object(byte[] blob) {
        Object obj = new Object();

        ObjectInputStream bin;
        try {
            bin = new ObjectInputStream(new ByteArrayInputStream(blob));
            obj = bin.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
