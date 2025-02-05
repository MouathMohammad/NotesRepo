package qa.gov.mol.utils.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(name = "ContractServlet", urlPatterns = { "/contractservlet" })
public class ContractServlet extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE);
        PrintWriter out = response.getWriter();
        out.println("<p>&nbsp;</p>\n" + "\n" + "<h2 style=\"text-align:right\">اصدار ترخيص مكتب استقدام</h2>\n" + "\n" +
                    "<h3 style=\"text-align:right\">معالي / وزير العمل حفظه الله</h3>\n" + "\n" +
                    "<h3 style=\"text-align:right\">قام مقدم الطلب، بتقديم طلب إصدار رخصة مكتب استقدام عمالة من الخارج لحساب الغير، وتالياً بيانات الطلب:</h3>\n" +
                    "\n" + "<h3 style=\"text-align:right\">تفاصيل الطلب:</h3>\n" + "\n" + "<p><br />\n" +
                    "بيانات المكتب:</p>\n" + "\n" + "<hr />\n" +
                    "<table style=\"float:right; height:52px; width:100%\">\n" + "	<tbody>\n" + "		<tr>\n" +
                    "			<td style=\"height:50px; text-align:right; width:50%\">اسم المكتب:\n" + "			<p>$</p>\n" +
                    "			</td>\n" + "			<td style=\"height:10px; text-align:right; width:30px\">رقم قيد المنشأة:\n" +
                    "			<p>$</p>\n" + "			</td>\n" + "		</tr>\n" + "		<tr>\n" +
                    "			<td style=\"height:50px; text-align:right; width:50%\">الرقم الشخصي للمرخص له:\n" +
                    "			<p>$</p>\n" + "			</td>\n" +
                    "			<td style=\"height:10px; text-align:right; width:30px\">اسم المرخص له:\n" + "			<p>$</p>\n" +
                    "			</td>\n" + "		</tr>\n" + "		<tr>\n" +
                    "			<td style=\"height:10px; text-align:right; width:30px\">عمر المرخص له:\n" +
                    "			<p>$ سنة</p>\n" + "			</td>\n" + "		</tr>\n" + "	</tbody>\n" + "</table>\n" + "\n" +
                    "<p>&nbsp;</p>\n" + "\n" + "<p>رأي مدير الادارة:</p>\n" + "\n" + "<hr />\n" +
                    "<table style=\"float:right; height:52px; width:100%\">\n" + "	<tbody>\n" + "		<tr>\n" +
                    "			<td style=\"height:50px; text-align:right; width:30%\">توصية مدير الادارة:\n" +
                    "			<p>$</p>\n" + "			</td>\n" +
                    "			<td style=\"height:10px; text-align:right; width:70%\">ملاحظات مدير الادارة:\n" +
                    "			<p>$</p>\n" + "			</td>\n" + "		</tr>\n" + "	</tbody>\n" + "</table>\n" + "\n" +
                    "<p>&nbsp;</p>\n" + "\n" + "<p>&nbsp;</p>\n" + "\n" + "<p>رأي الوكيل المساعد:</p>\n" + "\n" +
                    "<hr />\n" + "<table style=\"float:right; height:52px; width:100%\">\n" + "	<tbody>\n" +
                    "		<tr>\n" + "			<td style=\"height:50px; text-align:right; width:30%\">توصية الوكيل المساعد:\n" +
                    "			<p>$</p>\n" + "			</td>\n" +
                    "			<td style=\"height:10px; text-align:right; width:70%\">ملاحظات الوكيل المساعد:\n" +
                    "			<p>$</p>\n" + "			</td>\n" + "		</tr>\n" + "	</tbody>\n" + "</table>\n" + "\n" +
                    "<p>&nbsp;</p>\n" + "\n" + "<p>$</p>\n");
        out.close();
    }
}
