package mol.webservice.client;


import java.math.BigDecimal;

import java.util.Base64;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import mol.eservices.shared.model.shcprc.am.MolShcPrcAppModuleImpl;
import mol.eservices.shared.model.shcprc.vo.rvo.PrcVwscTrxSmrzdRecRVOImpl;
import mol.eservices.shared.model.shcprc.vo.rvo.PrcVwscTrxSmrzdRecRVORowImpl;

import mol.eservices.shared.model.shcprc.vo.rvo.SmzrdRecTasksVoImpl;
import mol.eservices.shared.model.shcprc.vo.rvo.SmzrdRecTasksVoRowImpl;

import mol.webservice.client.cloud.workflow.WorkflowAPIService;
import mol.webservice.client.cloud.workflow.WorkflowAPIService_Service;
import mol.webservice.client.cloud.workflow.types.UpdateTaskOutcomeRequest;
import mol.webservice.client.cloud.workflow.types.UpdateTaskOutcomeResults;
import mol.webservice.client.osb.sms.types.SendIndividualMessageRequest;
import mol.webservice.client.types.CheckinFileResult;
import mol.webservice.client.types.UCMFile;
import mol.webservice.client.types.UpdateTaskRequest;
import mol.webservice.client.types.UpdateTaskResponse;
import mol.webservice.client.osb.ucm.ExecuteBindQSService;
import mol.webservice.client.osb.ucm.ExecutePtt;
import mol.webservice.client.osb.ucm.types.CheckinFileRequest;
import mol.webservice.client.osb.ucm.types.CheckinFileResponse;
import mol.webservice.client.osb.ucm.types.GetFileByIDResult;
import mol.webservice.client.osb.ucm.types.GetFileByIdRequest;
import mol.webservice.client.osb.ucm.types.IdcFile;
import mol.webservice.client.soa.notification.SendNotification;
import mol.webservice.client.soa.notification.SendnotificationClientEp;
import mol.webservice.client.soa.notification.types.Attachment;
import mol.webservice.client.soa.notification.types.ObjectFactory;
import mol.webservice.client.soa.notification.types.SendMailRequest;
import mol.webservice.client.utils.CommonUtil;

import oracle.adf.share.ADFContext;

import oracle.jbo.*;
import oracle.jbo.client.Configuration;
import oracle.jbo.domain.Number;


public class WebServiceClient {

    public WebServiceClient() {
        super();
    }

    public static void main(String args[]) {
        try {
            //            UCMFile file = new UCMFile();
            //            file.setAuthor("weblogic");
            //            file.setDocName("Document");
            //            file.setDocTitle("Document");
            //            file.setDocType("Document");
            //            String encodedPdf = "";
            //            file.setFileContent("123".getBytes());
            //            file.setFileName("Document.pdf");
            //            CheckinFileResult result = checkinDocument(file);
            //            UCMFile resultFile = getUcmFileById(17660);
            //            List<UCMFile> attachments = new ArrayList<UCMFile>();
            //            attachments.add(resultFile);
            //            ;
            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setLocale("ar");
            request.setOutcome("APPROVE");
            request.setUsername("mol_rec_employee_oid_local");
            request.setTaskNumber(203321);
            UpdateTaskResponse response = updateTaskOutcomePrem(request);
            System.out.println(response.getResultCode());
            System.out.println(response.getResultMessage());
            //            sendIndividualSms("33611270", "Test SMS");
            //            sendEmail("t.abuhamra@mscmena.com", null, null, "Subject", "body", attachments);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     *
     * @param file of type UCMFile
     * @return
     */
    public static CheckinFileResult checkinDocument(UCMFile file) {
        CheckinFileResult fileResult = new CheckinFileResult();
        ExecuteBindQSService executeBindQSService = new ExecuteBindQSService();
        ExecutePtt executePtt = executeBindQSService.getExecuteBindQSPort();
        CheckinFileRequest request = new CheckinFileRequest();
        request.setDDocAuthor(file.getAuthor());
        request.setDDocName(file.getDocName());
        request.setDDocTitle(file.getDocTitle());
        request.setDDocType(file.getDocType());
        request.setDSecurityGroup("Public");
        IdcFile pFile = new IdcFile();
        pFile.setFileName(file.getFileName());
        pFile.setFileContent(file.getFileContent());
        request.setPrimaryFile(pFile);
        CheckinFileResponse response = executePtt.checkinFile(request);
        fileResult.setContentId(response.getStatusInfo().getStatusMessage());
        fileResult.setDid(response.getDID());
        fileResult.setResult(response.getStatusInfo().getStatusMessage());
        fileResult.setResultCode(response.getStatusInfo().getStatusCode());

        return fileResult;
    }


    public static UCMFile getUcmFileById(int dID) {
        UCMFile ucmFile = new UCMFile();
        ExecuteBindQSService executeBindQSService = new ExecuteBindQSService();
        ExecutePtt executePtt = executeBindQSService.getExecuteBindQSPort();
        GetFileByIdRequest request = new GetFileByIdRequest();
        request.setDID(dID);
        request.setRendition("WEB");
        GetFileByIDResult response = executePtt.getFile(request);
        if (response.getFileInfo() != null)
            ucmFile.setAuthor(response.getFileInfo()
                                      .get(0)
                                      .getDDocAuthor());
        if (response.getFileInfo() != null)
            ucmFile.setDocName(response.getFileInfo()
                                       .get(0)
                                       .getDDocName());
        if (response.getFileInfo() != null)
            ucmFile.setDocTitle(response.getFileInfo()
                                        .get(0)
                                        .getDDocTitle());
        if (response.getFileInfo() != null)
            ucmFile.setDocType(response.getFileInfo()
                                       .get(0)
                                       .getDDocType());
        ucmFile.setFileName(response.getDownloadFile().getFileName());
        ucmFile.setFileContent(response.getDownloadFile().getFileContent());
        return ucmFile;
    }


    public static UpdateTaskResponse updateTaskOutcome(UpdateTaskRequest updateRequest) {
        UpdateTaskResponse response = new UpdateTaskResponse();
        ADFContext oldContext = ADFContext.findCurrent();
        System.out.println("updateTaskOutcome current oldContext: " + oldContext);
        String amDef = "mol.eservices.shared.model.shcprc.am.MolShcPrcAppModule";
        String config = "MolShcPrcAppModuleLocal";
        MolShcPrcAppModuleImpl am = null;
        try {

            am = (MolShcPrcAppModuleImpl) Configuration.createRootApplicationModule(amDef, config);
            SmzrdRecTasksVoImpl vo = (SmzrdRecTasksVoImpl) am.findViewObject("SmzrdRecTasksVo1");
            vo.ensureVariableManager().setVariableValue("p_task_number", new BigDecimal(updateRequest.getTaskNumber()));
            vo.setOrderByClause("RECORD_ID DESC");
            vo.executeQuery();
            SmzrdRecTasksVoRowImpl row = (SmzrdRecTasksVoRowImpl) vo.first();
            System.out.println("WebServiceClient.updateTaskOutcome - Records ID: " + row.getRecordId() +
                               ", Trx Source: " + row.getTrxSource() + ", Task number: " + row.getTaskNumber());
            if (row != null &&
                row.getTrxSource() ==
 2) {
                // Cloud Request
                UpdateTaskResponse result = updateTaskOutcomeCloud(updateRequest);
                response.setResultCode(result.getResultCode());
                response.setResultMessage(result.getResultMessage());


            } else if (row != null) {
                //Prem Request
                UpdateTaskResponse result = updateTaskOutcomePrem(updateRequest);
                response.setResultCode(result.getResultCode());
                response.setResultMessage(result.getResultMessage());

            } else {
                response.setResultCode(500);
                response.setResultMessage("لا يمكن اتخاذ الاجراء، لم يتم العثور على الطلب");
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Work with your appmodule and view object here
            if (am != null)
                Configuration.releaseRootApplicationModule(am, true);
            //            ADFContext.resetADFContext(oldContext);
        }
        return response;
    }

    public static UpdateTaskResponse updateTaskOutcomePrem(UpdateTaskRequest updateRequest) {
        UpdateTaskResponse response = new UpdateTaskResponse();
        try {
            System.out.println("Update Prem Task: " + updateRequest.getTaskNumber() + ", by: " + updateRequest.getUsername() + ", action: " + updateRequest.getOutcome());
            //Prem Request
            mol.webservice.client.workflow.WorkflowAPIService_Service workflowAPIService_Service = new mol.webservice.client.workflow.WorkflowAPIService_Service();
            mol.webservice.client.workflow.WorkflowAPIService workflowAPIService = workflowAPIService_Service.getWorkflowAPIService();
            mol.webservice.client.workflow.types.UpdateTaskOutcomeRequest request = new  mol.webservice.client.workflow.types.UpdateTaskOutcomeRequest();
            request.setLocale(updateRequest.getLocale());
            request.setOutcome(updateRequest.getOutcome());
            request.setTaskNumber(updateRequest.getTaskNumber());
            request.setUsername(updateRequest.getUsername());
             mol.webservice.client.workflow.types.UpdateTaskOutcomeResults result = workflowAPIService.updateTaskOutcome(request);
            response.setResultCode(result.getResultCode());
            response.setResultMessage(result.getResultMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static UpdateTaskResponse updateTaskOutcomeCloud(UpdateTaskRequest updateRequest) {
        UpdateTaskResponse response = new UpdateTaskResponse();
        System.out.println("Update Cloud Task: " + updateRequest.getTaskNumber() + ", by: " + updateRequest.getUsername() + ", action: " + updateRequest.getOutcome());
        try {
            WorkflowAPIService_Service workflowAPIService_Service = new WorkflowAPIService_Service();
            WorkflowAPIService workflowAPIService = workflowAPIService_Service.getWorkflowAPIService();
            UpdateTaskOutcomeRequest taskDetails = new UpdateTaskOutcomeRequest();
            taskDetails.setLocale(updateRequest.getLocale());
            taskDetails.setOutcome(updateRequest.getOutcome());
            taskDetails.setTaskNumber(updateRequest.getTaskNumber());
            taskDetails.setUsername(updateRequest.getUsername());
            UpdateTaskOutcomeResults result = workflowAPIService.updateTaskOutcome(taskDetails);
            response.setResultCode(result.getResultCode());
            response.setResultMessage(result.getResultMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static void sendIndividualSms(String mobileNo, String message) {
        mol.webservice.client.osb.sms.ExecuteBindQSService executeBindQSService =
            new mol.webservice.client.osb.sms.ExecuteBindQSService();
        mol.webservice.client.osb.sms.ExecutePtt executePtt = executeBindQSService.getExecuteBindQSPort();

        SendIndividualMessageRequest reqeust = new SendIndividualMessageRequest();
        reqeust.setMessage(message);
        reqeust.setMobileNo(mobileNo);
        executePtt.sendIndividualMessage(reqeust);

    }

    public static int reassignTask(String username, int taskNumber, String toUser) {
        try {
            WorkflowAPIService_Service workflowAPIService_Service = new WorkflowAPIService_Service();
            WorkflowAPIService workflowAPIService = workflowAPIService_Service.getWorkflowAPIService();
            UpdateTaskOutcomeRequest request = new UpdateTaskOutcomeRequest();
            request.setLocale("ar");
            request.setOutcome("");
            request.setTaskNumber(taskNumber);
            request.setUsername(username);
            UpdateTaskOutcomeResults result = workflowAPIService.reassignTask(request, toUser);
            return result.getResultCode();
        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        }
    }


    public static int sendEmail(String to, String cc, String bcc, String subject, String body,
                                List<UCMFile> attachments) {
        ObjectFactory factory = new ObjectFactory();
        SendnotificationClientEp sendnotificationClientEp = new SendnotificationClientEp();
        SendNotification sendNotification = sendnotificationClientEp.getSendNotificationPt();

        SendMailRequest request = factory.createSendMailRequest();

        request.setTo(to);

        request.setBcc(factory.createSendMailRequestBcc(bcc));
        request.setCc(factory.createSendMailRequestCc(cc));
        request.setSubject(subject);
        request.setBody(body);


        List<Attachment> attachmentsElement = request.getAttachments();
        for (UCMFile attachment : attachments) {
            Attachment att = new Attachment();
            System.out.println(attachment.getFileName().substring(attachment.getFileName().indexOf(".") + 1));
            JAXBElement attachmentName = factory.createAttachmentAttachmentName(attachment.getFileName());
            JAXBElement fileContent =
                factory.createAttachmentFileContent(Base64.getEncoder().encodeToString(attachment.getFileContent()));
            JAXBElement mimeType =
                factory.createAttachmentMimeType(getMimeTypeByFileExtension(attachment.getFileName()
                                                                            .substring(attachment.getFileName()
                                                                                       .indexOf(".") + 1)));
            System.out.println(mimeType.getValue());
            att.setAttachmentName(attachmentName);
            att.setFileContent(fileContent);
            att.setMimeType(mimeType);
            attachmentsElement.add(att);
        }

        sendNotification.process(request);
        return 1;
    }

    private static String getMimeTypeByFileExtension(String extension) {
        String extensionName = extension.toLowerCase();
        if (extensionName != null) {
            if (extensionName.equals("png") || extensionName.equals("jpg") || extensionName.equals("bmp") ||
                extensionName.equals("gif") || extensionName.equals("jpeg"))
                return "image/" + extensionName;

            if (extensionName.equals("txt") || extensionName.equals("csv") || extensionName.equals("html") ||
                extensionName.equals("htm") || extensionName.equals(""))
                return "text/" + extensionName;

            switch (extension.toLowerCase()) {
            case "pdf":
                return "application/pdf";
            case "xml":
                return "application/xml";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "doc":
                return "application/msword";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }
        } else
            return "text/plain";
        return "text/plain";
    }
}


