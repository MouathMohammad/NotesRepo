package qa.gov.mol.utils.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import java.math.BigDecimal;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.sql.Connection;
import java.sql.DriverManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import javax.faces.application.NavigationHandler;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import mol.eservices.shared.model.shcprc.vo.ev.RepVwscRequestsRegistryUVOImpl;
import mol.eservices.shared.model.shcprc.vo.ev.RepVwscRequestsRegistryUVORowImpl;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.design.JasperDesign;

import net.sf.jasperreports.engine.xml.JRXmlLoader;

import oracle.adf.controller.ControllerContext;
import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.share.security.SecurityContext;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.component.rich.data.RichTreeTable;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.binding.AttributeBinding;
import oracle.binding.OperationBinding;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.RowIterator;
import oracle.jbo.client.Configuration;
import oracle.jbo.domain.BlobDomain;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.uicli.binding.JUCtrlHierBinding;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;
import oracle.jbo.uicli.binding.JUCtrlHierTypeBinding;
import oracle.jbo.uicli.binding.JUCtrlListBinding;
import oracle.jbo.uicli.binding.JUIteratorBinding;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.CollectionModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.UploadedFile;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

import qa.gov.mol.utils.db.DbUtil;
//import org.apache.poi.hssf.usermodel.HSSFCellStyle;
//import org.apache.poi.hssf.util.HSSFColor;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.Font;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.xssf.streaming.SXSSFSheet;
//import org.apache.poi.xssf.streaming.SXSSFWorkbook;
//import org.apache.poi.xssf.usermodel.XSSFCell;


public class CommonUtil implements Serializable {
    String validityURL;
    String validtyFlag;
    private static ApplicationModule am = null;
    private static final String COMMON_SERVICES_URL =
        "http://HOSTNAME/soa-infra/services/default/CoopServices/commonservices_client_ep?wsdl#%7Bhttp%3A%2F%2Fxmlns.oracle.com%2FMosalBPMApp%2FCoopServices%2Fcommonservices%7Dcommonservices_client_ep";
    private static final String COOP_SERVICES_URL =
        "http://HOSTNAME/soa-infra/services/default/CoopServices/coopservicesclient_ep?WSDL#%7Bhttp%3A%2F%2Fxmlns.oracle.com%2FCoopDevApp%2FCoopServices%2Fcoopservices%7Dcoopservicesclient_ep";
    private static final String MOCI_SERVICE_URL =
        "http://HOSTNAME/soa-infra/services/default/MOCI_Integ/mociintegclient_client_ep?WSDL#%7Bhttp%3A%2F%2Fxmlns.oracle.com%2FMosalIntegrationApplication%2FMOCI_Integ%2FMociIntegClient%7Dmociintegclient_client_ep";
    private static final String PACI_SERVICE_URL =
        "http://HOSTNAME/soa-infra/services/default/PaciIntegProj/paciinteg_client_ep?WSDL#%7Bhttp%3A%2F%2Fxmlns.oracle.com%2FMosalIntegrationApplication%2FIntegProjcet%2FPaciInteg%7Dpaciinteg_client_ep";

    public CommonUtil() {
        super();
    }


    public static boolean isNumeric(String s) {
        return java.util
                   .regex
                   .Pattern
                   .matches("\\d+", s);
    }

    /**
     *
     * @param ReportName = the report name
//     * @param id   = process id
     */
    public static void generateReport(String ReportName, Map paramMap) {
        DbUtil dbUtil = new DbUtil();
        String username = "";
        if (JSFUtils.resolveExpression("#{sessionScope.userName}") != null)
            username = (String) JSFUtils.resolveExpression("#{sessionScope.userName}");
        Map result = dbUtil.logReportGeneration(username, ReportName, paramMap.toString());
        String reportNumber = result.get("RESULT_CODE").toString();
        if (!reportNumber.equals("-1"))
            paramMap.put("report_ref_number", reportNumber);
        paramMap.put("requester_user_name", username);
        String newPage = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
        request.getSession().removeAttribute("paramMap");
        request.getSession().setAttribute("paramMap", paramMap);
        request.getSession().removeAttribute("datasource");
        request.getSession().setAttribute("datasource", "");
        request.getSession().removeAttribute("reportName");
        request.getSession().setAttribute("reportName", ReportName);

        try {
            newPage = getOhsHostPath() + "shc/rtf/generatefilereport";
        } catch (Exception e) {
        }

        ExtendedRenderKitService erks = Service.getService(ctx.getRenderKit(), ExtendedRenderKitService.class);
        String url =
            "window.open('" + newPage +
            "','_blank','toolbar=yes,scrollbars=yes,resizable=yes,top=0,left=0,width=700,height=700');";
        erks.addScript(FacesContext.getCurrentInstance(), url);

    }


    public static void redirectToURLExternalWindow(String url) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExtendedRenderKitService erks = Service.getService(ctx.getRenderKit(), ExtendedRenderKitService.class);
        String windowUrl =
            "window.open('" + url +
            "','_blank','toolbar=yes,scrollbars=yes,resizable=yes,top=0,left=0,width=700,height=700');";
        erks.addScript(FacesContext.getCurrentInstance(), windowUrl);
    }


    public static void browseFile(int did, String contentType) {
        String newPage = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
        request.getSession().removeAttribute("did");
        request.getSession().setAttribute("did", did);
        request.getSession().removeAttribute("contentType");
        request.getSession().setAttribute("contentType", contentType);
        //        newPage = getApplicationURL() + "browsefile?did=" + did + "&contentType=" + contentType;

        try {
            newPage = getOhsHostPath() + "shc/rtf/browsefile?did=" + did + "&contentType=" + contentType;
            System.out.println("newPage: " + newPage);
        } catch (Exception e) {
        }

        ExtendedRenderKitService erks = Service.getService(ctx.getRenderKit(), ExtendedRenderKitService.class);
        String url =
            "window.open('" + newPage +
            "','_blank','toolbar=yes,scrollbars=yes,resizable=yes,top=0,left=0,width=700,height=700');";
        erks.addScript(FacesContext.getCurrentInstance(), url);
    }

    public static void downloadFile(int did, String fileType, String fileName) {
        String newPage = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
        request.getSession().removeAttribute("did");
        request.getSession().setAttribute("did", did);
        request.getSession().removeAttribute("fileType");
        request.getSession().setAttribute("fileType", fileType);
        request.getSession().removeAttribute("fileName");
        request.getSession().setAttribute("fileName", fileName);
        newPage = getApplicationURL() + "downloadfile";
        ExtendedRenderKitService erks = Service.getService(ctx.getRenderKit(), ExtendedRenderKitService.class);
        String url = "window.open('" + newPage + "','_blank');";
        erks.addScript(FacesContext.getCurrentInstance(), url);
    }

    public static void previewReport(BigDecimal productId, String LanguageCode, Map paramMap) {

        //Get username from session
        String userName = CommonUtil.getUserName();
        if (userName != null)
            paramMap.put("requestedBy", userName);

        //Save Report view registry to DB
        String amDef = "mol.eservices.shared.model.shcprc.am.MolShcPrcAppModule";
        String config = "MolShcPrcAppModuleLocal";
        ApplicationModuleImpl amImpl = (ApplicationModuleImpl) Configuration.createRootApplicationModule(amDef, config);
        try {
            RepVwscRequestsRegistryUVOImpl repVwscRequestsRegistryUVO =
                (RepVwscRequestsRegistryUVOImpl) amImpl.findViewObject("RepVwscRequestsRegistryUVO1");
            RepVwscRequestsRegistryUVORowImpl row =
                (RepVwscRequestsRegistryUVORowImpl) repVwscRequestsRegistryUVO.createRow();
            row.setProductId(productId);
            row.setLanguageCode(LanguageCode);
            //StringBuilder sb = new StringBuilder();
            //sb.append("Report Parameters=[");

            if (paramMap.get("ProcessId") != null){
                row.setProcessId(new BigDecimal(paramMap.get("ProcessId").toString()));
                //sb.append("ProcessId" + " = '" + String.valueOf(paramMap.get("ProcessId")) + "\' ,");

            }
            if (paramMap.get("ProcTrxId") != null){
                row.setProcTrxId(new BigDecimal(paramMap.get("ProcTrxId").toString()));
                //sb.append("ProcTrxId" + " = '" + String.valueOf(paramMap.get("ProcTrxId")) + "\' ,");
            }
            if (paramMap.get("PkCode1") != null){
                row.setPkCode1(paramMap.get("PkCode1").toString());
                //sb.append("PkCode1" + " = '" + String.valueOf(paramMap.get("PkCode1")) + "\' ,");
            }
            if (paramMap.get("PkCode2") != null){
                row.setPkCode2(paramMap.get("PkCode2").toString());
                //sb.append("PkCode2" + " = '" + String.valueOf(paramMap.get("PkCode2")) + "\' ,");
            }
            
            //sb.append("]");
            //byte[] requestParamsArray = sb.toString().getBytes("UTF-8");
            
            //row.setRequestParams(new BlobDomain(requestParamsArray));

            ByteArrayOutputStream bObj = new ByteArrayOutputStream();
            ObjectOutputStream out;
            try {
                out = new ObjectOutputStream(bObj);
                if (paramMap != null) {
                    out.writeObject(paramMap);
                    out.close();
                    bObj.close();
                    byte[] byteOut = bObj.toByteArray();
                    BlobDomain blobDomain = new BlobDomain(byteOut);
                    row.setRequestParams(blobDomain);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            repVwscRequestsRegistryUVO.insertRow(row);
            amImpl.getTransaction().commit();

            FacesContext ctx = FacesContext.getCurrentInstance();

            String newPage = null;

            try {
                newPage = getOhsHostPath() + "shc/rtf/previewreport?REGISTRY_ID=" + row.getRegistryId();
                System.out.println("newPage: " + newPage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ExtendedRenderKitService erks = Service.getService(ctx.getRenderKit(), ExtendedRenderKitService.class);
            String url =
                "window.open('" + newPage +
                "','_blank','toolbar=yes,scrollbars=yes,resizable=yes,top=0,left=0,width=700,height=700');";
            System.out.println("previewReport: " + url);
            amImpl.getTransaction().disconnect();
            erks.addScript(FacesContext.getCurrentInstance(), url);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean releaseApplicationModule(ApplicationModule appModule) {
        try {
            System.out.println("Relese App Module");
            Configuration.releaseRootApplicationModule(appModule, true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void viewMosaProfileRootReport(String rootReportPath, Map paramMap) {
        String newPage = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
        request.getSession().removeAttribute("rootReportPath");
        request.getSession().setAttribute("rootReportPath", rootReportPath);
        request.getSession().removeAttribute("datasource");
        request.getSession().setAttribute("datasource", "jdbc/mosa_profile");
        request.getSession().removeAttribute("paramMap");
        request.getSession().setAttribute("paramMap", paramMap);
        newPage = getApplicationURL() + "viewfile";
        ExtendedRenderKitService erks = Service.getService(ctx.getRenderKit(), ExtendedRenderKitService.class);
        String url =
            "window.open('" + newPage +
            "','_blank','toolbar=yes,scrollbars=yes,resizable=yes,top=0,left=0,width=700,height=700');";
        erks.addScript(FacesContext.getCurrentInstance(), url);
        //erks.addScript(FacesContext.getCurrentInstance(), "window.print();");
    }

    public static String getApplicationURL() {
        String url = "";
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                                                                          .getExternalContext()
                                                                          .getRequest();
            url = request.getRequestURL().toString();

            if (url.indexOf("faces") == -1) {
                url = url.substring(0, url.indexOf("rr")) + "faces/";
                if (url.contains("ohs.mol.gov.qa")) {
                    //                    url = url.replace("ohs.mol.gov.qa", getOhsHost());

                }
            } else {
                url = url.substring(0, url.indexOf("faces"));
            }
            System.out.println(" ************ getApplicationURL: " + url);
        } catch (Exception e) {
            url = "";
        }
        return url;
    }

    public static String getRequestRootURL() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        String url = ctx.getExternalContext().getRequestScheme() + "://";
        url = url + ctx.getExternalContext().getRequestServerName();
        url = url + ":" + ctx.getExternalContext().getRequestServerPort() + "/";

        return url;
    }

    public static String getUserName() {
        String currentUserID = ADFContext.getCurrent()
                                         .getSecurityContext()
                                         .getUserName();
        return currentUserID;
    }


    public static int getLovIDByCodeAndParent(String code, int parent) {
        int lovId = new DbUtil().getLovByCodeAndName(code, parent);
        return lovId;
    }


    /**
     * method for storing Session variables
     * @param key object key
     * @param object value to store
     */
    public static void storeOnSession(String key, Object object) {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            Map sessionState = ctx.getExternalContext().getSessionMap();
            sessionState.put(key, object);
        } catch (Exception e) {
            // TODO: Add catch code
            JSFUtils.addFacesInfoMessage(e.getMessage());
        }

    }

    /**
     * method for getting Session variables
     *
     * @param key object key
     */
    public static Object getFromSession(String key) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map sessionState = ctx.getExternalContext().getSessionMap();
        return sessionState.get(key);
    }


    public static ArrayList<Object> getMemebershipDetails(String civilNum, int coopId) {
        ArrayList<Object> details = new ArrayList<Object>();
        try {
            details = new DbUtil().getMemeberDetails(civilNum, coopId);

        } catch (Exception e) {
            // TODO: Add catch code
            e.printStackTrace();
        }

        return details;
    }

    public static int validateMemberNumber(int memberNumber, int coopId) {
        int details = new DbUtil().dBvalidateMemberNumber(memberNumber, coopId);
        return details;
    }


    public static HashMap validateMemberJoining(String civilNum, int coopId, Date transDate) {
        return new DbUtil().dBvalidateMemberJoining(civilNum, coopId, transDate);
    }

    public static ArrayList validateWithdrawal(String civilNum, int coopId) {
        return new DbUtil().dBvalidateWithdrawal(civilNum, coopId);
    }

    public static void multiCheckBoxChange(ValueChangeEvent valueChangeEvent, String attributeName,
                                           Hashtable<Integer, BigDecimal> hashTable) {
        ArrayList<Integer> newValue = new ArrayList<Integer>();
        newValue = (ArrayList<Integer>) valueChangeEvent.getNewValue();
        //ArrayList<Integer> oldValue = new ArrayList<Integer>();
        //oldValue = (ArrayList<Integer>)valueChangeEvent.getOldValue();
        if (newValue == null || newValue.size() < 0) {
            CommonUtil.getAttributeBinding(attributeName).setInputValue("");
        } else {
            //String value=(String)getAttributeBinding(attributeName).getInputValue();
            //String  [] splittedValue=value.split("-");
            String attributeValue = null;
            //attributeValue=splittedValue[0];
            attributeValue = hashTable.get(newValue.get(0)).toString();
            for (int index = 1; index < newValue.size(); index++) {
                attributeValue = attributeValue + "-" + hashTable.get(newValue.get(index)).toString();
            }
            CommonUtil.getAttributeBinding(attributeName).setInputValue(attributeValue);
        }
    }

    public static void addRadioSelecteditemToList(String juCTRLListBindingName, String viewRowId, String attributeName,
                                                  List<Integer> radioSelectedList,
                                                  Hashtable<Integer, BigDecimal> selectedItemHashTable) {

        JUCtrlListBinding ListBinding =
            (JUCtrlListBinding) CommonUtil.getBinding().get(juCTRLListBindingName); //QA_OPERATION_DEV_RESOURCES_vo1
        RowIterator set = ListBinding.getRowIterator();

        //Row row1 = set.getCurrentRow();
        Row[] row = ListBinding.getAllRowsInRange();
        String value = (String) CommonUtil.getAttributeBinding(attributeName).getInputValue();
        //if(value != null){
        //String  [] splittedValue=value.split("-");
        for (int index = 0; index < row.length; index++) {
            BigDecimal numberValue = (BigDecimal) row[index].getAttribute(viewRowId);
            String strValue = numberValue.toString();
            if (value != null) {
                String[] splittedValue = value.split("-");
                for (int index1 = 0; index1 < splittedValue.length; index1++) {
                    String splitstr = splittedValue[index1];
                    if (splitstr.equals(strValue)) {
                        if (radioSelectedList == null) {
                            radioSelectedList = new ArrayList<Integer>();
                        }
                        radioSelectedList.add(index);
                        break;
                    }
                }
            }
            if (selectedItemHashTable.size() != row.length) {
                selectedItemHashTable.put(index, numberValue);
            }
        }
    }

    public static void handleTreeSelection(SelectionEvent selectionEvent, String el) {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ELContext elctx = fctx.getELContext();
        ExpressionFactory exprFactory = fctx.getApplication().getExpressionFactory();

        MethodExpression me =
            exprFactory.createMethodExpression(elctx, el, Object.class, new Class[] { SelectionEvent.class });
        me.invoke(elctx, new Object[] { selectionEvent });

    }

    public static SecurityContext getSecurityContext() {
        return ADFContext.getCurrent().getSecurityContext();
    }

    public static DCBindingContainer getBinding() {
        return (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
    }

    public static void navigateWithOperations(String navigation, String operation) {
        if (operation != null) {
            OperationBinding operBinding = getBinding().getOperationBinding(operation);
            operBinding.execute();
        }
        NavigationHandler nvHndlr = FacesContext.getCurrentInstance()
                                                .getApplication()
                                                .getNavigationHandler();
        nvHndlr.handleNavigation(FacesContext.getCurrentInstance(), null, navigation);
    }

    public static void redirectToView(String viewId) {
        FacesContext fCtx = FacesContext.getCurrentInstance();
        ExternalContext eCtx = fCtx.getExternalContext();

        String activityUrl = ControllerContext.getInstance().getGlobalViewActivityURL(viewId);
        try {
            eCtx.redirect(activityUrl);
        } catch (IOException e) {
            e.printStackTrace();
            JSFUtils.addFacesErrorMessage("Exception when redirect to " + viewId);
        }
    }

    public static void navigateToFragment(String navigation) {
        NavigationHandler nvHndlr = FacesContext.getCurrentInstance()
                                                .getApplication()
                                                .getNavigationHandler();
        nvHndlr.handleNavigation(FacesContext.getCurrentInstance(), null, navigation);
    }

    public static void TableSelection(SelectionEvent selectionEvent) {
        RichTable tree1 = (RichTable) selectionEvent.getSource();
        RowKeySet rks2 = selectionEvent.getAddedSet();
        Iterator rksIterator = rks2.iterator();
        if (rksIterator.hasNext()) {
            List key = (List) rksIterator.next();
            JUCtrlHierBinding treeBinding = null;
            treeBinding = (JUCtrlHierBinding) ((CollectionModel) tree1.getValue()).getWrappedData();
            JUCtrlHierNodeBinding nodeBinding = nodeBinding = treeBinding.findNodeByKeyPath(key);
            DCIteratorBinding _treeIteratorBinding = null;
            _treeIteratorBinding = treeBinding.getDCIteratorBinding();
            JUIteratorBinding iterator = nodeBinding.getIteratorBinding();
            String keyStr = nodeBinding.getRowKey().toStringFormat(true);
            iterator.setCurrentRowWithKey(keyStr);

            JUCtrlHierTypeBinding typeBinding = nodeBinding.getHierTypeBinding();
            String targetIteratorSpelString = typeBinding.getTargetIterator();
            if (targetIteratorSpelString != null && !targetIteratorSpelString.isEmpty()) {
                DCIteratorBinding targetIterator = resolveTargetIterWithSpel(targetIteratorSpelString);
                Key rowKey = nodeBinding.getCurrentRow().getKey();
                targetIterator.setCurrentRowWithKey(rowKey.toStringFormat(true));
            }
        }
    }

    public static void treeTableSelection(SelectionEvent selectionEvent) {
        RichTreeTable tree1 = (RichTreeTable) selectionEvent.getSource();
        RowKeySet rks2 = selectionEvent.getAddedSet();
        Iterator rksIterator = rks2.iterator();
        if (rksIterator.hasNext()) {
            List key = (List) rksIterator.next();
            JUCtrlHierBinding treeBinding = null;
            treeBinding = (JUCtrlHierBinding) ((CollectionModel) tree1.getValue()).getWrappedData();
            JUCtrlHierNodeBinding nodeBinding = nodeBinding = treeBinding.findNodeByKeyPath(key);
            DCIteratorBinding _treeIteratorBinding = null;
            _treeIteratorBinding = treeBinding.getDCIteratorBinding();
            JUIteratorBinding iterator = nodeBinding.getIteratorBinding();
            String keyStr = nodeBinding.getRowKey().toStringFormat(true);
            iterator.setCurrentRowWithKey(keyStr);

            JUCtrlHierTypeBinding typeBinding = nodeBinding.getHierTypeBinding();
            String targetIteratorSpelString = typeBinding.getTargetIterator();
            if (targetIteratorSpelString != null && !targetIteratorSpelString.isEmpty()) {
                DCIteratorBinding targetIterator = resolveTargetIterWithSpel(targetIteratorSpelString);
                Key rowKey = nodeBinding.getCurrentRow().getKey();
                targetIterator.setCurrentRowWithKey(rowKey.toStringFormat(true));
            }
        }
    }

    public static DCIteratorBinding resolveTargetIterWithSpel(String spelExpr) {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ELContext elctx = fctx.getELContext();
        ExpressionFactory elFactory = fctx.getApplication().getExpressionFactory();
        ValueExpression valueExpr = elFactory.createValueExpression(elctx, spelExpr, Object.class);
        DCIteratorBinding dciter = (DCIteratorBinding) valueExpr.getValue(elctx);
        return dciter;
    }

    public static HttpSession getSessionMap() {
        FacesContext fctx = FacesContext.getCurrentInstance();
        //HttpServletRequest request=(HttpServletRequest)fctx.getExternalContext();
        ExternalContext ectx = fctx.getExternalContext();
        HttpSession userSession = (HttpSession) ectx.getSession(true);
        return userSession;
    }

    public static AttributeBinding getAttributeBinding(String name) {
        AttributeBinding attOtherReasonAr = (AttributeBinding) getBinding().getControlBinding(name);
        return attOtherReasonAr;
    }

    public static void addPartial(UIComponent component) {
        AdfFacesContext context = AdfFacesContext.getCurrentInstance();
        context.addPartialTarget(component);
    }

    /**
     *
     * @param BundleName
     * @param key
     * @return
     */
    public static String getResourceBundleByKey(String BundleName, String key) {
        FacesContext fc = FacesContext.getCurrentInstance();
        Locale locale = fc.getViewRoot().getLocale();
        //        Locale locale = new Locale("en");

        ResourceBundle bundle = ResourceBundle.getBundle(BundleName, locale);
        return bundle.getString(key);

    }

    public static String invokeActionSubmit() {
        Map map = FacesContext.getCurrentInstance()
                              .getExternalContext()
                              .getRequestMap();
        oracle.bpel.services.workflow.worklist.adf.InvokeActionBean invokeActionBean =
            (oracle.bpel.services.workflow.worklist.adf.InvokeActionBean) map.get("invokeActionBean");
        return invokeActionBean.invokeOperation();
    }

    public static void main(String[] args) {
        //        CommonservicesClientEp commonservicesClientEp = new CommonservicesClientEp();
        //        Commonservices commonservices = commonservicesClientEp.getCommonservicesPt();
        //        CheckInDocumentProcess process = new CheckInDocumentProcess();
        //        CheckInDocumentProcess.FileDetails fileDetails = new CheckInDocumentProcess.FileDetails();
        //        fileDetails.setFileName("Test File");
        //        byte[] s = new byte[1];
        //        fileDetails.setFileName("fname");
        //        fileDetails.setFileContent(s);
        //        process.setDocTitle("docTitle");
        //        process.setDocId("contentID");
        //        CheckInDocumentProcessResponse response = commonservices.checkInDocument(process);


        System.out.println("Main started");
        try {
            // Load the Jasper design file
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con =
                DriverManager.getConnection("jdbc:oracle:thin:@STG-DB-LABSC12-SCAN.molsa.qatar:1526/stglabsec1",
                                            "BM_EWC", "mol#2022");
            // Create a map of report parameters
            Map<String, Object> parameters = new HashMap<>();

            parameters.put("text", "नमस�?ते");

            // Fill the report
            JasperPrint jasperPrint =
                JasperFillManager.fillReport("C:\\labsec\\reports\\final_products\\SHC\\TestHindi.jasper", parameters,
                                             con);

            // Export the report to PDF
            JasperExportManager.exportReportToPdfFile(jasperPrint,
                                                      "C:\\labsec\\reports\\final_products\\SHC\\TestHindi.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String validateAttachmentsBeforeSubmit(String processName, long applicationNumber,
                                                         long attachmentSubCode) {
        DbUtil dbUutil = new DbUtil();
        HashMap map = dbUutil.validateRequestAttachment(processName, attachmentSubCode, applicationNumber);
        if (map.get("RESULT").equals(1))
            return CommonUtil.invokeActionSubmit();
        else {
            JSFUtils.addFacesErrorMessage(map.get("MESSAGE").toString());
        }
        return "";
    }

    public static String getBPMURL() {
        String hostname;
        String bpmUrl = "bpm.mol.gov.qa";

        try {
            hostname = getHostname().toUpperCase();
            System.out.println("Get BPM URL FROM: " + hostname);
            if (hostname.startsWith("PRD-"))
                bpmUrl += ":8008";
            else if (hostname.contains("-PRD-QC-"))
                bpmUrl += ":8001";
            else if (hostname.startsWith("STG-"))
                bpmUrl += ":8001";
            else if (hostname.contains("-STG-QC-"))
                bpmUrl += ":8001";
            else if (hostname.startsWith("DEV-"))
                bpmUrl += ":8001";
            else if (hostname.contains("-DEV-QC-"))
                bpmUrl += ":8001";
            else if (hostname.startsWith("MOL-APP-83"))
                bpmUrl += ":8001";
            else if (hostname.startsWith("VM-BPM-STAG") || hostname.startsWith("VM-OSB-STAG"))
                bpmUrl = "bpm-cstg.mol.gov.qa";
            else if (hostname.startsWith("VM-BPM-DEV") || hostname.startsWith("VM-OSB-DEV"))
                bpmUrl = "bpm-cdev.mol.gov.qa";
            else if (hostname.startsWith("VM-BPM-PROD") || hostname.startsWith("VM-OSB-PROD"))
                bpmUrl = "bpm-cprd.mol.gov.qa";
            else
                bpmUrl = "bpm.mol.gov.qa:8001";
        } catch (Exception e) {
            bpmUrl += ":8001";
        }
        System.out.println("Get BPM URL: " + bpmUrl);
        return bpmUrl;
    }

    public static String getBPMURLPrem() {
        String hostname;

        String bpmUrl = "prem.bpm.mol.gov.qa";
        try {
            hostname = getHostname().toUpperCase();
            System.out.println("Get PREM BPM URL FROM: " + hostname);
            if (hostname.startsWith("PRD-"))
                bpmUrl += ":8008";
            else if (hostname.contains("-PRD-QC-"))
                bpmUrl += ":8001";
            else if (hostname.startsWith("STG-"))
                bpmUrl += ":8001";
            else if (hostname.contains("-STG-QC-"))
                bpmUrl += ":8001";
            else if (hostname.startsWith("DEV-"))
                bpmUrl += ":8001";
            else if (hostname.contains("-DEV-QC-"))
                bpmUrl += ":8001";
            else if (hostname.startsWith("MOL-APP-83"))
                bpmUrl += ":8001";
            else if (hostname.startsWith("VM-BPM-STAG") || hostname.startsWith("VM-OSB-STAG"))
                bpmUrl = ":8001";
            else if (hostname.startsWith("VM-BPM-DEV") || hostname.startsWith("VM-OSB-DEV"))
                bpmUrl = ":8001";
            else if (hostname.startsWith("VM-BPM-PROD") || hostname.startsWith("VM-OSB-PROD"))
                bpmUrl = ":8001";
            else
                bpmUrl = "localhost:7101";
        } catch (Exception e) {
            bpmUrl += ":8001";
        }
        System.out.println("Get PREM BPM URL : " + bpmUrl);
        return bpmUrl;
    }

    public static String getSOAUrl() {
        String hostname;
        String soaUrl = "soa.mol.gov.qa";
        try {
            hostname = getHostname().toUpperCase();
            if (hostname.startsWith("PRD-"))
                soaUrl += ":8008";
            else if (hostname.contains("-PRD-QC-"))
                soaUrl += ":8001";
            else if (hostname.startsWith("STG-"))
                soaUrl += ":8001";
            else if (hostname.contains("-STG-QC-"))
                soaUrl += ":8001";
            else if (hostname.startsWith("DEV-"))
                soaUrl += ":8001";
            else if (hostname.contains("-DEV-QC-"))
                soaUrl += ":8001";
            else if (hostname.startsWith("MOL-APP-83"))
                soaUrl += ":8001";
            else if (hostname.startsWith("VM-BPM-STAG") || hostname.startsWith("VM-OSB-STAG"))
                soaUrl = "soa-cstg.mol.gov.qa";
            else if (hostname.startsWith("VM-BPM-DEV") || hostname.startsWith("VM-OSB-DEV"))
                soaUrl = "soa-cstg.mol.gov.qa";
            else if (hostname.startsWith("VM-BPM-PROD") || hostname.startsWith("VM-OSB-PROD"))
                soaUrl = "soa-cprd.mol.gov.qa";
            else
                soaUrl = "localhost:7101";
        } catch (Exception e) {
            soaUrl += ":8001";
        }
        return soaUrl;
    }

    public static String getOSBUrl() {
        String hostname;
        String osbUrl = "osb.mol.gov.qa";
        try {
            hostname = getHostname().toUpperCase();
            if (hostname.startsWith("PRD-"))
                osbUrl += ":9008";
            else if (hostname.startsWith("STG-"))
                osbUrl += ":9001";
            else if (hostname.startsWith("DEV-"))
                osbUrl += ":8005";
            else if (hostname.startsWith("MOL-APP-83"))
                osbUrl += ":8005";
            else if (hostname.startsWith("VM-BPM-STAG") || hostname.startsWith("VM-OSB-STAG"))
                osbUrl = "osb-cstg.mol.gov.qa";
            else if (hostname.startsWith("VM-BPM-DEV") || hostname.startsWith("VM-OSB-DEV"))
                osbUrl = "osb-cdev.mol.gov.qa";
            else if (hostname.startsWith("VM-BPM-PROD") || hostname.startsWith("VM-OSB-PROD"))
                osbUrl = "osb-cprd.mol.gov.qa";
            else
                osbUrl += ":8005";
        } catch (Exception e) {
            osbUrl += ":8005";
        }
        return osbUrl;
    }

    public static String getHostname() throws Exception {
        String hostname;
        hostname = java.net
                       .InetAddress
                       .getLocalHost()
                       .getHostName()
                       .toUpperCase();
        return hostname;
    }

    public static String getOhsHost() throws Exception {
        String hostname = getHostname();
        if (hostname.equalsIgnoreCase("MOL-APP-83"))
            hostname = "mscjo.dynalias.com";
        if (hostname.equalsIgnoreCase("DEV-BPM"))
            hostname = "ws-dev.mol.gov.qa";
        if (hostname.equalsIgnoreCase("STG-BPM-01") || hostname.equalsIgnoreCase("STG-BPM-02"))
            hostname = "ws-stg.mol.gov.qa";
        if (hostname.equalsIgnoreCase("PRD-BPM-01") || hostname.equalsIgnoreCase("PRD-BPM-02"))
            hostname = "ws.mol.gov.qa";
        if (hostname.toUpperCase().startsWith("VM-BPM-STAG") || hostname.toUpperCase().startsWith("VM-OSB-STAG"))
            hostname = "ws-cstg.mol.gov.qa";
        if (hostname.toUpperCase().startsWith("VM-BPM-DEV") || hostname.toUpperCase().startsWith("VM-OSB-DEV"))
            hostname = "ws-cdev.mol.gov.qa";
        if (hostname.toUpperCase().startsWith("VM-BPM-PROD") || hostname.toUpperCase().startsWith("VM-OSB-PROD"))
            hostname = "ws-cprd.mol.gov.qa";
        return hostname;
    }

    public static String getOhsHostPath() throws Exception {
        String hostname = getHostname();
        if (hostname.equalsIgnoreCase("MOL-APP-83"))
            hostname = "http://mscjo.dynalias.com:7778/";
        else if (hostname.equalsIgnoreCase("DEV-BPM"))
            hostname = "http://ws-dev.mol.gov.qa:7777/";
        else if (hostname.equalsIgnoreCase("STG-BPM-01") || hostname.equalsIgnoreCase("STG-BPM-02"))
            hostname = "http://ws-stg.mol.gov.qa:7777/";
        else if (hostname.equalsIgnoreCase("PRD-BPM-01") || hostname.equalsIgnoreCase("PRD-BPM-02"))
            hostname = "https://ws.mol.gov.qa/";
        else if (hostname.toUpperCase().startsWith("VM-BPM-STAG") || hostname.toUpperCase().startsWith("VM-OSB-STAG"))
            hostname = "https://ws-cstg.mol.gov.qa/";
        else if (hostname.toUpperCase().startsWith("VM-BPM-DEV") || hostname.toUpperCase().startsWith("VM-OSB-DEV"))
            hostname = "https://ws-cdev.mol.gov.qa/";
        else if (hostname.toUpperCase().startsWith("VM-BPM-PROD") || hostname.toUpperCase().startsWith("VM-OSB-PROD"))
            hostname = "https://ws-cprd.mol.gov.qa/";
        else
            hostname = "http://mscjo.dynalias.com:7778/";
        return hostname;
    }

    public static String getCurrentHost() {
        String hostname = "";
        try {
            //Get Current Server Host Name
            hostname = java.net
                           .InetAddress
                           .getLocalHost()
                           .getHostName();
        } catch (UnknownHostException f) {
            //Failed to get the hostname, then get request server name from the request,
            //and resolve the request server name to the corresponding IP

        }
        return hostname;
    }

    public static BlobDomain createBlobDomain(UploadedFile file) {
        // init the internal variables
        InputStream in = null;
        BlobDomain blobDomain = null;
        OutputStream out = null;

        try {
            in = file.getInputStream();
            blobDomain = new BlobDomain();
            out = blobDomain.getBinaryOutputStream();
            org.apache
               .commons
               .io
               .IOUtils
               .copy(in, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blobDomain;
    }

    public static BlobDomain createBlobDomain(byte[] blobBytes) {
        BlobDomain blobDomain = null;
        OutputStream out = null;
        try {
            blobDomain = new BlobDomain(blobBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blobDomain;
    }
    //    public static ByteArrayOutputStream getResultSetAsExcel(ResultSet rs, String sheetName) {
    //        SXSSFWorkbook workbook = new SXSSFWorkbook(-1);
    //        Sheet sheet = workbook.createSheet(sheetName);
    //        try {
    //            writeHeaderLine(rs, sheet);
    //            writeDataLines(rs, sheet);
    //            ByteArrayOutputStream os = new ByteArrayOutputStream();
    //            workbook.write(os);
    //            return os;
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        return null;
    //    }
    //
    //    private static void writeHeaderLine(ResultSet result, Sheet sheet) throws SQLException {
    //        // write header line containing column names
    //        ResultSetMetaData metaData = result.getMetaData();
    //        int numberOfColumns = metaData.getColumnCount();
    //
    //        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
    //        // exclude the first column which is the ID field
    //
    //        for (int i = 1; i <= numberOfColumns; i++) {
    //            try {
    //                String columnName = metaData.getColumnName(i);
    //                if (metaData.getColumnLabel(i) != null && !metaData.getColumnLabel(i).isEmpty())
    //                    columnName = metaData.getColumnLabel(i);
    //                sheet.setColumnWidth(i - 1, 2000);
    //                Cell headerCell = headerRow.createCell(i - 1);
    //                headerCell.setCellValue(columnName);
    //                headerCell.setCellType(XSSFCell.CELL_TYPE_STRING);
    //                headerCell.setCellStyle(createHeadrStyle(sheet));
    //                sheet.autoSizeColumn(i);
    //            } catch (Exception e) {
    //            }
    //
    //        }
    //    }
    //
    //    private static CellStyle createHeadrStyle(Sheet sheet) {
    //        CellStyle style = sheet.getWorkbook().createCellStyle();
    //        Font font = sheet.getWorkbook().createFont();
    //        font.setColor(HSSFColor.WHITE.index);
    //        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
    //        style.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
    //        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
    //        style.setFont(font);
    //        style.setAlignment(CellStyle.ALIGN_CENTER);
    //        //        style.setBorderBottom(CellStyle.BORDER_DOUBLE);
    //        //        style.setBorderLeft(CellStyle.BORDER_DOUBLE);
    //        //        style.setBorderRight(CellStyle.BORDER_DOUBLE);
    //        //        style.setBorderTop(CellStyle.BORDER_DOUBLE);\
    //
    //        return style;
    //    }
    //
    //    private static void writeDataLines(ResultSet result, Sheet sheet) throws SQLException {
    //        ResultSetMetaData metaData = result.getMetaData();
    //        int numberOfColumns = metaData.getColumnCount();
    //        int rowCount = 1;
    //        CellStyle oddRowStyle = createOddRowStyle(sheet);
    //        try {
    //            while (result.next()) {
    //                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowCount++);
    //                for (int i = 1; i <= numberOfColumns; i++) {
    //                    Object valueObject = result.getObject(i);
    //                    int rowType = metaData.getColumnType(i);
    //                    Cell cell = row.createCell(i - 1);
    //                    cell.setCellStyle(oddRowStyle);
    //                    if (rowType == 2) {
    //                        BigDecimal bd1 = new BigDecimal((valueObject + ""));
    //                        Double d2 = bd1.doubleValue();
    //                        cell.setCellValue(d2);
    //                    } else
    //                        cell.setCellValue((String) valueObject);
    //                    if (rowCount % 100 == 0 && i + 1 == numberOfColumns) {
    //                        ((SXSSFSheet) sheet).flushRows(); // retain 100 last rows and flush all others
    //                    }
    //                }
    //            }
    //        } catch (Exception e) {
    //            // TODO: Add catch code
    //            e.printStackTrace();
    //        }
    //
    //    }
    //
    //    private static CellStyle createOddRowStyle(Sheet sheet) {
    //        // oddRowStyle
    //        CellStyle oddRowStyle = sheet.getWorkbook().createCellStyle();
    //        Font font = sheet.getWorkbook().createFont();
    //        font.setColor(HSSFColor.BLACK.index);
    //        //        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
    //        //        oddRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
    //        //        oddRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
    //        oddRowStyle.setFont(font);
    //        oddRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
    //        //        oddRowStyle.setBorderBottom(CellStyle.BORDER_DOUBLE);
    //        //        oddRowStyle.setBorderLeft(CellStyle.BORDER_DOUBLE);
    //        //        oddRowStyle.setBorderRight(CellStyle.BORDER_DOUBLE);
    //        //        oddRowStyle.setBorderTop(CellStyle.BORDER_DOUBLE);
    //
    //        return oddRowStyle;
    //    }


}
