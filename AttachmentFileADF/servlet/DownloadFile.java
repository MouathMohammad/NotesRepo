package qa.gov.mol.utils.servlet;


import java.io.Serializable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

//import mol.webservice.client.WebServiceClient;
//import mol.webservice.client.types.UCMFile;


//@WebServlet(name = "DownloadFile", urlPatterns = { "/downloadfile" })
public class DownloadFile extends HttpServlet implements Serializable {
    @SuppressWarnings("compatibility:5006384921022707745")
    private static final long serialVersionUID = -7812272770786248258L;
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    //    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //
    //        int did = 0;
    //        String fileType = "";
    //        String fileName = "";
    //        try {
    //            did = (Integer) request.getSession().getAttribute("did");
    //            fileType = (String) request.getSession().getAttribute("fileType");
    //            fileName = (String) request.getSession().getAttribute("fileName");
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //
    //
    //        try {
    //            ServletOutputStream outputStream = response.getOutputStream();
    //            UCMFile file = WebServiceClient.getUcmFileById(did);
    //            try {
    //                LogUtil.logEservice(LogUtil.INFO, "Download File: " + file.getFileName() + ", File UCM DID: " + did,
    //                                    this.getClass().getName(), LogUtil.DOWNLOAD);
    //            } catch (Exception e) {
    //            }
    //            response.setContentType(fileType);
    //            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    //            outputStream.write(file.getFileContent());
    //            outputStream.close();
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            response.setContentType(CONTENT_TYPE);
    //            PrintWriter out = response.getWriter();
    //            out.println("<html>");
    //            out.println("<head><title>تحميل مل�? غير صحيح</title></head>");
    //            out.println("<body>");
    //            out.println("<p>نعتذر لقد حصل خطأ أثناء تحميل هذا المل�?، سيتم اخطار مسؤول النظام لحل المشكلة �?ي أقرب وقت ممكن</p>");
    //            out.println("</body></html>");
    //            out.close();
    //        }
    //    }
}
