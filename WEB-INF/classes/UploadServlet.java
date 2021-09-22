package net.codejava.servlet;
import java.io.File;
import java.util.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
@WebServlet("/UploadServlet")
@MultipartConfig(fileSizeThreshold=1024*1024*2, // 2MB
                 maxFileSize=1024*1024*10,      // 10MB
                 maxRequestSize=1024*1024*50)   // 50MB
public class UploadServlet extends HttpServlet {
    /**
     * Name of the directory where uploaded files will be saved, relative to
     * the web application directory.
     */
    private static final String SAVE_DIR = "download";
     
    /**
     * handles file upload
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // gets absolute path of the web application
        String appPath = request.getServletContext().getRealPath("");
        // constructs path of the directory to save uploaded file
        String savePath = appPath + File.separator + SAVE_DIR;
         
        // creates the save directory if it does not exists
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }
        print_out(request);
        for (Part part : request.getParts()) {
            String fileName = extractFileName(part);
            // refines the fileName in case it is an absolute path
            fileName = new File(fileName).getName();
            part.write(savePath + File.separator + fileName);
        }
        request.setAttribute("message", "Upload has been done successfully!");
        getServletContext().getRequestDispatcher("/message.jsp").forward(
                request, response);
    }
    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            System.out.println(s);
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }
        return "";
    }

    /**
     * Print the entire request to std out.
     * */
    private void print_out (HttpServletRequest request){
        System.out.println("#############POST REQUEST###############");
        Enumeration<String> params = request.getParameterNames(); 
        String uname="", projname = "";
        if (params != null && !params.isEmpty()) {
          Map<String,String> parameterMap = splitQuery(params);
          if (parameterMap.containsKey("user_name")) {
            uname = parameterMap.get("user_name");
          }
          if (parameterMap.containsKey("project")) {
            projname = parameterMap.get("project");
          }
          
        }
        if(!uname.equals("")){
            System.out.println("user_name recieved: "+uname);
        }
        if(!projname.equals("")){
            System.out.println("projname recieved: "+projname);
        }
        // while(params.hasMoreElements()){
        //      String paramName = params.nextElement();
        //      System.out.println("Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
        // }
    }
}