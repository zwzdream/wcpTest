package com.farm.doc.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.farm.core.page.ViewMode;
import com.farm.doc.domain.FarmDocfile;
import com.farm.doc.domain.FarmDoctext;
import com.farm.doc.domain.ex.DocEntire;
import com.farm.doc.exception.CanNoReadException;
import com.farm.doc.exception.DocNoExistException;
import com.farm.doc.server.FarmDocManagerInter;
import com.farm.doc.server.FarmFileManagerInter;
import com.farm.doc.server.FarmFileManagerInter.FILE_TYPE;
import com.farm.parameter.FarmParameterService;
import com.farm.web.WebUtils;

/**
 * 文档管理
 * 
 * @author autoCode
 * 
 */
@RequestMapping("/actionImg")
@Controller
public class ActionImgQuery extends WebUtils {
	private static final Logger log = Logger.getLogger(ActionImgQuery.class);

	@Resource
	private FarmFileManagerInter farmFileManagerImpl;
	@Resource
	private FarmDocManagerInter farmDocManagerImpl;

	/**
	 * 上传附件文件
	 * 
	 * @return
	 */
	@RequestMapping(value = "/PubFPupload.do")
	@ResponseBody
	public Map<String, Object> upload(@RequestParam(value = "imgFile", required = false) MultipartFile file,
			HttpServletRequest request, ModelMap model, HttpSession session) {
		int error;
		String message;
		String url = null;
		String id = null;
		String fileName = "";
		try {
			String fileid = null;
			// 验证大小
			String maxLength = FarmParameterService.getInstance().getParameter("config.doc.upload.length.max");
			if (file.getSize() > Integer.valueOf(maxLength)) {
				throw new Exception("文件不能超过" + Integer.valueOf(maxLength) / 1024 + "kb");
			}
			CommonsMultipartFile cmFile = (CommonsMultipartFile) file;
			DiskFileItem item = (DiskFileItem) cmFile.getFileItem();
			{// 小于8k不生成到临时文件，临时解决办法。zhanghc20150919
				if (!item.getStoreLocation().exists()) {
					item.write(item.getStoreLocation());
				}
			}

			fileName = URLEncoder.encode(item.getName(), "utf-8");
			// 验证类型
			List<String> types = parseIds(FarmParameterService.getInstance().getParameter("config.doc.upload.types")
					.toUpperCase().replaceAll("，", ","));
			if (!types.contains(file.getOriginalFilename()
					.substring(file.getOriginalFilename().lastIndexOf(".") + 1, file.getOriginalFilename().length())
					.toUpperCase())) {
				throw new Exception("文件类型错误，允许的类型为：" + FarmParameterService.getInstance()
						.getParameter("config.doc.upload.types").toUpperCase().replaceAll("，", ","));
			}
			fileid = farmFileManagerImpl.saveFile(item.getStoreLocation(), FILE_TYPE.HTML_INNER_IMG,
					file.getOriginalFilename(), getCurrentUser(session));
			error = 0;
			url = farmFileManagerImpl.getFileURL(fileid);
			message = "";
			id = fileid;
		} catch (Exception e) {
			e.printStackTrace();
			error = 1;
			message = e.getMessage();
		}
		return ViewMode.getInstance().putAttr("error", error).putAttr("url", url).putAttr("message", message)
				.putAttr("id", id).putAttr("fileName", fileName).returnObjMode();
	}

	/**
	 * 上传图片文件
	 * 
	 * @return
	 */
	@RequestMapping(value = "/PubFPuploadImg.do")
	@ResponseBody
	public Map<String, Object> PubFPuploadImg(@RequestParam(value = "imgFile", required = false) MultipartFile file,
			HttpServletRequest request, ModelMap model, HttpSession session) {
		int error;
		String message;
		String url = null;
		String id = null;
		String fileName = "";
		try {
			String fileid = null;
			// 验证大小
			String maxLength = FarmParameterService.getInstance().getParameter("config.doc.upload.length.max");
			if (file.getSize() > Integer.valueOf(maxLength)) {
				throw new Exception("文件不能超过" + Integer.valueOf(maxLength) / 1024 + "kb");
			}
			CommonsMultipartFile cmFile = (CommonsMultipartFile) file;
			DiskFileItem item = (DiskFileItem) cmFile.getFileItem();
			{// 小于8k不生成到临时文件，临时解决办法。zhanghc20150919
				if (!item.getStoreLocation().exists()) {
					item.write(item.getStoreLocation());
				}
			}

			fileName = URLEncoder.encode(item.getName(), "utf-8");
			// 验证类型
			List<String> types = parseIds(FarmParameterService.getInstance().getParameter("config.doc.img.upload.types")
					.toUpperCase().replaceAll("，", ","));
			if (!types.contains(file.getOriginalFilename()
					.substring(file.getOriginalFilename().lastIndexOf(".") + 1, file.getOriginalFilename().length())
					.toUpperCase())) {
				throw new Exception("文件类型错误，允许的类型为：" + FarmParameterService.getInstance()
						.getParameter("config.doc.img.upload.types").toUpperCase().replaceAll("，", ","));
			}
			fileid = farmFileManagerImpl.saveFile(item.getStoreLocation(), FILE_TYPE.HTML_INNER_IMG,
					file.getOriginalFilename(), getCurrentUser(session));
			error = 0;
			url = farmFileManagerImpl.getFileURL(fileid);
			message = "";
			id = fileid;
		} catch (Exception e) {
			//e.printStackTrace();
			error = 1;
			message = e.getMessage();
		}
		return ViewMode.getInstance().putAttr("error", error).putAttr("url", url).putAttr("message", message)
				.putAttr("id", id).putAttr("fileName", fileName).returnObjMode();
	}

	/**
	 * 根据fileid下载文件
	 * 
	 * @return
	 */
	@RequestMapping("/Publoadfile")
	public void download(String id, HttpServletRequest request, HttpServletResponse response) {
		FarmDocfile file = null;
		try {
			file = farmFileManagerImpl.getFile(id);
		} catch (Exception e) {
			file = null;
		}
		if (file == null || file.getFile() == null || !file.getFile().exists()) {
			file = new FarmDocfile();
			file.setFile(farmFileManagerImpl.getNoneImg());
			file.setName("none");
		}
		response.setCharacterEncoding("utf-8");
		response.setContentType("multipart/form-data");
		try {
			response.setHeader("Content-Disposition",
					"attachment;fileName=" + new String(file.getName().getBytes("gbk"), "iso-8859-1"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}

		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(file.getFile());
			os = response.getOutputStream();
			byte[] b = new byte[2048];
			int length;
			while ((length = is.read(b)) > 0) {
				os.write(b, 0, length);
			}
		} catch (FileNotFoundException e) {
			InputStream is1 = null;
			OutputStream os1 = null;
			try {
				String webPath = FarmParameterService.getInstance().getParameter("farm.constant.webroot.path");
				String filePath = "/WEB-FACE/img/style/nullImg.png".replaceAll("/",
						File.separator.equals("/") ? "/" : "\\\\");
				File nullFile = new File(webPath + filePath);
				is1 = new FileInputStream(nullFile);
				os1 = response.getOutputStream();
				byte[] b = new byte[2048];
				int length;
				while ((length = is1.read(b)) > 0) {
					os1.write(b, 0, length);
				}

			} catch (Exception e1) {
				log.error(e.getMessage());
			} finally {
				try {
					is1.close();
					os1.close();
				} catch (IOException e1) {
					log.error(e.getMessage());
				}
			}
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			// 这里主要关闭。
			try {
				is.close();
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage());
			}
		}
	}
	
	/**
	 * 根据docid下载文件
	 * 
	 * @return
	 */
	@RequestMapping(value ="/PubloadfileDoc{docid}")
	public void downloadByDocid(@PathVariable("docid") String docid, HttpSession session, 
			HttpServletRequest request, HttpServletResponse response) {
		DocEntire doc=null;
		List<File> downLoadFiles=new ArrayList<File>();
		List<String> fileNames=new ArrayList<String>();
		try {
			doc = farmDocManagerImpl.getDoc(docid, getCurrentUser(session));
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		for (FarmDocfile node : doc.getFiles()) {
		
		FarmDocfile file = null;
		try {
			file = farmFileManagerImpl.getFile(node.getId());
		} catch (Exception e) {
			file = null;
		}
		if (file == null || file.getFile() == null || !file.getFile().exists()) {
			file = new FarmDocfile();
			file.setFile(farmFileManagerImpl.getNoneImg());
			file.setName("none");
		}
		 downLoadFiles.add(file.getFile());
		 fileNames.add(file.getName());
       }
		try {
			downLoadFiles(downLoadFiles,fileNames,request,response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
/*	@RequestMapping(value ="/PubloadfileDoc{docid}")
	public void downloadByDocid(@PathVariable("docid") String docid, HttpSession session, 
			HttpServletRequest request, HttpServletResponse response) {
		DocEntire doc=null;
		try {
			doc = farmDocManagerImpl.getDoc(docid, getCurrentUser(session));
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		for (FarmDocfile node : doc.getFiles()) {
			
			FarmDocfile file = null;
			try {
				file = farmFileManagerImpl.getFile(node.getId());
			} catch (Exception e) {
				file = null;
			}
			if (file == null || file.getFile() == null || !file.getFile().exists()) {
				file = new FarmDocfile();
				file.setFile(farmFileManagerImpl.getNoneImg());
				file.setName("none");
			}
			response.setCharacterEncoding("utf-8");
			response.setContentType("multipart/form-data");
			try {
				response.setHeader("Content-Disposition",
						"attachment;fileName=" + new String(file.getName().getBytes("gbk"), "iso-8859-1"));
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
			}
			
			InputStream is = null;
			OutputStream os = null;
			try {
				is = new FileInputStream(file.getFile());
				os = response.getOutputStream();
				byte[] b = new byte[2048];
				int length;
				while ((length = is.read(b)) > 0) {
					os.write(b, 0, length);
				}
			} catch (FileNotFoundException e) {
				InputStream is1 = null;
				OutputStream os1 = null;
				try {
					String webPath = FarmParameterService.getInstance().getParameter("farm.constant.webroot.path");
					String filePath = "/WEB-FACE/img/style/nullImg.png".replaceAll("/",
							File.separator.equals("/") ? "/" : "\\\\");
					File nullFile = new File(webPath + filePath);
					is1 = new FileInputStream(nullFile);
					os1 = response.getOutputStream();
					byte[] b = new byte[2048];
					int length;
					while ((length = is1.read(b)) > 0) {
						os1.write(b, 0, length);
					}
					
				} catch (Exception e1) {
					log.error(e.getMessage());
				} finally {
					try {
						is1.close();
						os1.close();
					} catch (IOException e1) {
						log.error(e.getMessage());
					}
				}
				log.error(e.getMessage());
			} catch (IOException e) {
				log.error(e.getMessage());
			} finally {
				// 这里主要关闭。
				try {
					is.close();
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error(e.getMessage());
				}
			}
		}	
		
	}
*/	
	/**
     * 把接受的全部文件打成压缩包 
     * @param List<File>;  
     * @param org.apache.tools.zip.ZipOutputStream  
     */
    public static void zipFile
            (List files, List names,ZipOutputStream outputStream) {
        int size = files.size();
        for(int i = 0; i < size; i++) {
            File file = (File) files.get(i);
            String name=(String)names.get(i);
            zipFile(file, name,outputStream);
        }
    }
    /**  
     * 根据输入的文件与输出流对文件进行打包
     * @param File
     * @param org.apache.tools.zip.ZipOutputStream
     */
    public static void zipFile(File inputFile,String name,
            ZipOutputStream ouputStream) {
        try {
            if(inputFile.exists()) {
                /**如果是目录的话这里是不采取操作的，
                 * 至于目录的打包正在研究中*/
                if (inputFile.isFile()) {
                    FileInputStream IN = new FileInputStream(inputFile);
                    BufferedInputStream bins = new BufferedInputStream(IN, 512);
                    //org.apache.tools.zip.ZipEntry
                    
                    ZipEntry entry = new ZipEntry(name);
                   // ZipEntry entry = new ZipEntry(inputFile.getName());
                    ouputStream.putNextEntry(entry);
                    // 向压缩文件中输出数据   
                    int nNumber;
                    byte[] buffer = new byte[512];
                    while ((nNumber = bins.read(buffer)) != -1) {
                        ouputStream.write(buffer, 0, nNumber);
                    }
                    // 关闭创建的流对象   
                    bins.close();
                    IN.close();
                } else {
                    try {
                        File[] files = inputFile.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            zipFile(files[i],name+i, ouputStream);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
  //文件打包下载
    public static HttpServletResponse downLoadFiles(List<File> files,List<String> names,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
    	  File file =new File("c:/中文名.rar");
        try {
            /**这个集合就是你想要打包的所有文件，
             * 这里假设已经准备好了所要打包的文件*/
            //List<File> files = new ArrayList<File>();
     
            /**创建一个临时压缩文件，
             * 我们会把文件流全部注入到这个文件中
             * 这里的文件你可以自定义是.rar还是.zip*/
        	 
        	if(files.size()>1){
        		  file= new File("c:/"+names.get(0).substring(0, names.get(0).indexOf("."))+"等多个文件"+".rar");
        	}else if(files.size()==1){
        		 file= new File("c:/"+names.get(0).substring(0, names.get(0).indexOf("."))+".rar");
        	}else if(files.size()<1){
        		response.setContentType("text/html;charset=utf-8");
        		response.getWriter().write("<script language='javascript'>alert('没有文档可以下载!');window.history.back();</script>");
        		return response;
        	}
       
            if (!file.exists()){   
                file.createNewFile();   
            }
            response.reset();
            //response.getWriter()
            //创建文件输出流
            FileOutputStream fous = new FileOutputStream(file);   
            /**打包的方法我们会用到ZipOutputStream这样一个输出流,
             * 所以这里我们把输出流转换一下*/
           ZipOutputStream zipOut 
            = new ZipOutputStream(fous);
            /**这个方法接受的就是一个所要打包文件的集合，
             * 还有一个ZipOutputStream*/
           zipFile(files,names, zipOut);
            zipOut.close();
            fous.close();
           return downloadZip(file,response);
        }catch (Exception e) {
                e.printStackTrace();
            }
            /**直到文件的打包已经成功了，
             * 文件的打包过程被我封装在FileUtil.zipFile这个静态方法中，
             * 稍后会呈现出来，接下来的就是往客户端写数据了*/
           
     
        return response ;
    }
    
    public static HttpServletResponse downloadZip(File file,HttpServletResponse response) {
        try {
        // 以流的形式下载文件。
        InputStream fis = new BufferedInputStream(new FileInputStream(file.getPath()));
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);
        fis.close();
        // 清空response
        response.reset();

        OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
        response.setContentType("application/octet-stream");

//如果输出的是中文名的文件，在此处就要进行处理
        response.setHeader("Content-Disposition", "attachment;filename=" +new String(file.getName().getBytes("gbk"), "iso-8859-1"));
        toClient.write(buffer);
        toClient.flush();
        toClient.close();
        } catch (IOException ex) {
        ex.printStackTrace();
        }finally{
             try {
                    File f = new File(file.getPath());
                    f.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return response;
    }
    

}
