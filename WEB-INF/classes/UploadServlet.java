package net.codejava.servlet;
import net.codejava.servlet.*;
import java.io.File;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.io.InputStream;
import java.io.BufferedReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import jakarta.servlet.http.Part;
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
        ServiceTypePDM serv_type = ServiceTypePDM.NON;
        String appPath = request.getServletContext().getRealPath("");
        String savePath = appPath + File.separator + SAVE_DIR;
         
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }
        String fileName = "";
        for (Part part : request.getParts()) {
            System.out.println("Part: "+part.getName());
            //get the purpose of the connection, and resolve
            switch(part.getName()){
                case "file":
                    String fileNameTmp = extractFileName(part);
                    fileName = fileNameTmp;
                    continue;
                case "user_name":
                    savePath = set_up_user_path(part,savePath);
                    continue;
                case "serv_type":
                    if(read_serv_type(part).equals("pdm_note_sync")){
                        serv_type=ServiceTypePDM.PDMNOTESYNC;
                        System.out.println("\t[Service Resolved] pdm note sync");
                    }
                    continue;
            }
        }
        switch(serv_type){
            case PDMNOTESYNC:
                save_the_file(fileName,savePath, request.getPart("file"));
                request.setAttribute("message", "Upload has been done successfully!");
                getServletContext().getRequestDispatcher("/message.jsp").forward(
                        request, response);
                break;
        }
    }

    /**
     *  Writes the file
     * 
     * */
    private void save_the_file(String fileName,String savePath, Part part )throws IOException{
        if (fileName.equals("pdm_rc.conf")){//config saving
            fileName = new File(fileName).getName();
            File configSaveDir = new File(savePath+File.separator+"config");
            if (!configSaveDir.exists()) {
                configSaveDir.mkdir();
            }
            part.write(savePath+File.separator+"config" 
                                            + File.separator + fileName);
        }
        else if(fileName!=""){
            fileName = new File(fileName).getName();
            part.write(savePath + File.separator + fileName);
        }
    }

    /**
     * Get the file path the file to be saved
     * */
    private String set_up_user_path(Part part,String savePath){
        String userName = extractUserName(part); 
        try{
            userName = read_user_name(part);
            if(!userName.equals("")){
                savePath = savePath + File.separator  + userName; // Checks if the user exists. Make a folder for the user if it doesn't
                File fileSaveDir = new File(savePath);
                if (!fileSaveDir.exists()) {
                    fileSaveDir.mkdir();
                    System.out.println("\t[Account Access]New user, access account for \""+userName+"\", saving in process...");
                }
                else {
                    System.out.println("\t[Account Access]Access account for \""+userName+"\", saving in process...");
                }
            }
        }
        catch(Exception e){
            System.out.println("\t[Account Access]get user name failure!");
        }
        return savePath;
    }

    /**
     * Gets the user name of the packet
     * */
    private String read_user_name(Part part)throws IOException{
        String user_str = "";
        if (!part.getName().equals("user_name")) return user_str;
        InputStream istream = part.getInputStream();
        int i;
        while((i = istream.read())!=-1) {
            user_str = user_str+(char)i;
        }
        return user_str;
    }

    /**
     * reads the type
     * */
    private String read_serv_type(Part part)throws IOException{
        String service = "";
        if (!part.getName().equals("serv_type")) return service;
        InputStream istream = part.getInputStream();
        int i;
        while((i = istream.read())!=-1) {
            service = service+(char)i;
        }
        return service;
    }

    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        // System.out.println(contentDisp);
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