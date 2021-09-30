package net.codejava.servlet;
import java.io.File;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.io.InputStream;
import java.io.BufferedReader;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
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

    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
      Map<String, String> query_pairs = new LinkedHashMap<String, String>();
      String[] pairs = query.split("&");
      for (String pair : pairs) {
        System.out.println(pair);
          int idx = pair.indexOf("=");
          query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
      }
      return query_pairs;
  } 
    /**
     * handles file upload
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        System.out.println("#############POST REQUEST###############");

        // gets absolute path of the web application
        String appPath = request.getServletContext().getRealPath("");
        // constructs path of the directory to save uploaded file
        String savePath = appPath + File.separator + SAVE_DIR;
         
        // creates the save directory if it does not exists
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }
        // print_out(request);
        for (Part part : request.getParts()) {
            // System.out.println(part.getName());
            String fileName = extractFileName(part);
            if(fileName.equals("")){
               String userName = extractUserName(part); 
               try{
                userName = read_user_name(part);
                if(!userName.equals("")){
                    savePath = savePath + File.separator  + userName; // Checks if the user exists. Make a folder for the user if it doesn't
                    fileSaveDir = new File(savePath);
                    if (!fileSaveDir.exists()) {
                        fileSaveDir.mkdir();
                    }
                }
                
                // part.write(savePath );
                System.out.println("Access account for \""+userName+"\", saving in process...");


               }
               catch(Exception e){
                System.out.println("get user name failure!");
               }
               continue;
            }
            // refines the fileName in case it is an absolute path
            fileName = new File(fileName).getName();
            part.write(savePath + File.separator + fileName);
        }
        request.setAttribute("message", "Upload has been done successfully!");
        getServletContext().getRequestDispatcher("/message.jsp").forward(
                request, response);
    }

    /**
     * Gets the user name of the packet
     * */
    private String read_user_name(Part part)throws IOException{
        String user_str = "";
        InputStream istream = part.getInputStream();
        // InputStreamReader ireader = new InputStreamReader(istream);
        // BufferedReader reader = new BufferedReader(ireader);
        // StringBuffer sb = new StringBuffer();
        // while((user_str = reader.readLine())!= null){
        //    sb.append(user_str);
        // }
        int i;
        while((i = istream.read())!=-1) {
         
            user_str = user_str+(char)i;
         }
        return user_str;
    }

    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        System.out.println(contentDisp);
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                // System.out.println(s);
                return s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }
        return "";
    }
    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractUserName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            // System.out.println(s);
            if (s.trim().startsWith("user_name")) {
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
        // Enumeration<String> params = request.getParameterNames(); 
        
        String params = request.getQueryString();
        String uname="", projname = "";
        System.out.println("full length form: "+params);

        if (params != null && !params.isEmpty()) {
          try{
            Map<String,String> parameterMap = splitQuery(params);
            if (parameterMap.containsKey("user_name")) {
              uname = parameterMap.get("user_name");
            }
            if (parameterMap.containsKey("project")) {
              projname = parameterMap.get("project");
            }
          }
          catch(Exception e){
            System.out.println("splitQuery failure");
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