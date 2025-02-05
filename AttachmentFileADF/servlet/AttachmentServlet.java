package qa.gov.mol.utils.servlet;


import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mol.webservice.client.WebServiceClient;
import mol.webservice.client.types.UCMFile;


public class AttachmentServlet extends HttpServlet implements Serializable {
    @SuppressWarnings("compatibility:-1329668886701688277")
    private static final long serialVersionUID = -8537978114261505065L;
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    /**
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * use this servlet to view a file from UCM, thiis servlet will fetch the file from the ucm and preview it in the browser
     * Mapping: /browsefile
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int did = 0;
        String contentType = "application/pdf";
        try {

            did = Integer.parseInt(request.getParameter("did"));
            contentType = request.getParameter("contentType");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ServletOutputStream outputStream = response.getOutputStream();
        try {
            response.setHeader("Cache-Control", "max-age=0");
            System.out.println("Content Type: " + did);
            System.out.println("Did: " + contentType);
            if (contentType.equalsIgnoreCase("jpg"))
                contentType = "	image/jpeg";
            response.setContentType(contentType);
            UCMFile file = WebServiceClient.getUcmFileById(did);
            outputStream.write(file.getFileContent());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("File browsed, flushed, and closed");
    }

    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
