package com.farm.doc.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.xmlbeans.XmlException;

import com.farm.doc.domain.FarmDoctype;
import com.farm.doc.domain.ex.DocEntire;
import com.farm.doc.server.commons.FarmDocFiles;
import com.farm.lucene.adapter.DocMap;

/**
 * 生成全文检索时辅助生成索引元数据对象
 * 
 * @author Administrator
 * 
 */
public class LuceneDocUtil {
	public static DocMap getDocMap(DocEntire doc) {
		String text = "";
		if (doc.getTexts() != null) {
			text = doc.getTexts().getText1();
		}
		text = HtmlUtils.HtmlRemoveTag(text);
		DocMap map = new DocMap(doc.getDoc().getId());
		String typeAll = "";
		// 拼接所有上级分类用于索引
		if (doc.getCurrenttypes() != null) {
			for (FarmDoctype node : doc.getCurrenttypes()) {
				if (typeAll.equals("")) {
					typeAll = node.getName();
				} else {
					typeAll = typeAll + "/" + node.getName();
				}
			}
			map.put("TYPENAME", typeAll, Store.YES, Index.ANALYZED);
			map.put("TYPEID", doc.getType().getId(), Store.YES, Index.NO);
		}
		map.put("FILECONTENT", doc.getFcontent(),Store.YES, Index.ANALYZED);
		map.put("TAGKEY", doc.getDoc().getTagkey(), Store.YES, Index.ANALYZED);
		map.put("TITLE", doc.getDoc().getTitle(), Store.YES, Index.ANALYZED);
		map.put("AUTHOR", doc.getDoc().getAuthor(), Store.YES, Index.ANALYZED);
		map.put("DOCDESCRIBE", doc.getDoc().getDocdescribe(), Store.YES, Index.ANALYZED);
		map.put("VISITNUM", "0", Store.YES, Index.ANALYZED);
		map.put("PUBTIME", doc.getDoc().getPubtime(), Store.YES, Index.ANALYZED);
		map.put("USERID", doc.getDoc().getCuser(), Store.YES, Index.ANALYZED);
		map.put("DOMTYPE", doc.getDoc().getDomtype(), Store.YES, Index.ANALYZED);
		map.put("TEXT", text, Store.YES, Index.ANALYZED);
		map.put("DOCID", doc.getDoc().getId(), Store.YES, Index.ANALYZED);
		return map;
	}

	public static DocMap convertFileMap(DocMap docmap, String fileId, String title, String text) {
		docmap.put("DOCID",docmap.getValue("ID"), Store.YES, Index.ANALYZED);
		docmap.put("ID", fileId, Store.YES, Index.ANALYZED);
		docmap.put("DOMTYPE", "file", Store.YES, Index.ANALYZED);
		docmap.put("TEXT", text, Store.YES, Index.ANALYZED);
		docmap.put("TITLE", title, Store.YES, Index.ANALYZED);
		return docmap;
	}
	
	
	/**
	  * Excel07 extractor
	  * @param fileName
	  * @param path
	  * @return
	  * @throws IOException
	  */
	 public static String ExcelXFileReader(String filePath) throws IOException {
		 InputStream path = new FileInputStream(filePath);
		 String content = null;
		 XSSFWorkbook wb = new XSSFWorkbook(path);
		 XSSFExcelExtractor extractor = new XSSFExcelExtractor(wb);
		 extractor.setFormulasNotResults(true);
		 extractor.setIncludeSheetNames(false);
		 content = extractor.getText();
		 return content;
	 }
	 
		
		/**
		  * Excel03 extractor
		  * @param fileName
		  * @param path
		  * @return
		  * @throws IOException
		  */
		 public static String ExcelFileReader(String filePath) throws IOException {
			 InputStream path = new FileInputStream(filePath);
			 String content = null;
			 HSSFWorkbook wb = new HSSFWorkbook(path);
			 ExcelExtractor extractor = new ExcelExtractor(wb);
			 extractor.setFormulasNotResults(true);
			 extractor.setIncludeSheetNames(false);
			 content = extractor.getText();
			 return content;
		 }
	 /**
	  * PDF extractor
	  * @param fileName
	  * @param path
	  * @return
	  * @throws Exception
	  */
	 public static String PdfboxFileReader(String filePath) throws Exception {
		 StringBuffer content = new StringBuffer("");
		 FileInputStream fis = new FileInputStream(filePath);
		 PDFParser p = new PDFParser(fis);
		 p.parse();
		 PDFTextStripper ts = new PDFTextStripper();
		 content.append(ts.getText(p.getPDDocument()));
		 fis.close();
		 p.getPDDocument().close();
		 return content.toString().trim();
	 }
	 /**
	  * PPt extractor
	  * @param fileName
	  * @param path
	  * @return
	  * @throws Exception
	  */
	 public static String PptFileReader(String filePath) throws Exception {

		 FileInputStream fis = new FileInputStream(filePath);
		 PowerPointExtractor extractor=new PowerPointExtractor(fis);
		 return extractor.getText();
	 }
	 
	 /**
	  * PPtx extractor
	  * @param fileName
	  * @param path
	  * @return
	  * @throws Exception
	  */
	 public static String PptxFileReader(String filePath) throws Exception {
		 XSLFPowerPointExtractor extractor = new XSLFPowerPointExtractor(POIXMLDocument.openPackage(filePath));
		 return extractor.getText();
	 }

	 /**
	  * word extractor
	  * @param fileName
	  * @param path
	  * @return
	  * @throws IOException
	 * @throws OpenXML4JException 
	 * @throws XmlException 
	  */
	 public static String WordFileReader(String filePath) throws IOException, XmlException, OpenXML4JException {
		 String bodyText = null;
		 if(filePath.endsWith(".doc")){
			 InputStream is = new FileInputStream(new File(filePath));
			 WordExtractor ex = new WordExtractor(is);
			 bodyText = ex.getText();
			 is.close();
		 }else{
			 bodyText = new XWPFWordExtractor(POIXMLDocument.openPackage(filePath)).getText();
		 }
		 return bodyText;
	 }
	 /**
	  * TXT extractor
	  * 
	  * @param fileName 
	  * @param charSet utf-8
	  * @return
	  * @throws IOException
	  */
	 public static String FileReaderAll(String filePath)throws IOException {
		 BufferedReader reader = null;
		 String code = getCharset(filePath);
		 if ("GBK".equals(code)) {			
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"GBK"));
		}else if("UTF-8".equals(code)){
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"UTF-8"));
		}else if("Unicode".equals(code)){
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"Unicode"));
		}else if("UTF-16BE".equals(code)){
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"UTF-16BE"));
		}
		 String line = new String();
		 String temp = new String();
		 while ((line = reader.readLine()) != null) {
			 temp += line;
		 }
		 reader.close();
		 return temp;
	 }
	 
/*	 @SuppressWarnings("resource")
	 private static String getCharset(String fileName) throws IOException{  	        
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));    
         int p = (bin.read() << 8) + bin.read();           
         String code = null;
         switch (p) {    
             case 0xefbb:    
                 code = "UTF-8";    
                 break;    
             case 0xfffe:    
                 code = "Unicode";    
                 break;    
             case 0xfeff:    
                 code = "UTF-16BE";    
                 break;    
             default:    
                 code = "GBK";    
         }    
         return code; 		
 }  */
	 
	   @SuppressWarnings("resource")
		private static  String getCharset(String sourceFile) {
			  String charset = "GBK";
			  byte[] first3Bytes = new byte[3];
			  try {
			   boolean checked = false;
			   BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
			   bis.mark(0);
			   int read = bis.read(first3Bytes, 0, 3);
			   if (read == -1) {
			    return charset; //文件编码为 ANSI
			   } else if (first3Bytes[0] == (byte) 0xFF
			     && first3Bytes[1] == (byte) 0xFE) {
			    charset = "UTF-16LE"; //文件编码为 Unicode
			    checked = true;
			   } else if (first3Bytes[0] == (byte) 0xFE
			     && first3Bytes[1] == (byte) 0xFF) {
			    charset = "UTF-16BE"; //文件编码为 Unicode big endian
			    checked = true;
			   } else if (first3Bytes[0] == (byte) 0xEF
			     && first3Bytes[1] == (byte) 0xBB
			     && first3Bytes[2] == (byte) 0xBF) {
			    charset = "UTF-8"; //文件编码为 UTF-8
			    checked = true;
			   }
			   bis.reset();
			   if (!checked) {
			    int loc = 0;
			    while ((read = bis.read()) != -1) {
			     loc++;
			     if (read >= 0xF0)
			      break;
			     if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
			      break;
			     if (0xC0 <= read && read <= 0xDF) {
			      read = bis.read();
			      if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
			       // (0x80
			       // - 0xBF),也可能在GB编码内
			       continue;
			      else
			       break;
			     } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
			      read = bis.read();
			      if (0x80 <= read && read <= 0xBF) {
			       read = bis.read();
			       if (0x80 <= read && read <= 0xBF) {
			        charset = "UTF-8";
			        break;
			       } else
			        break;
			      } else
			       break;
			     }
			    }
			   }
			   bis.close();
			  } catch (Exception e) {
			   e.printStackTrace();
			  }
			  return charset;
	   }
	
	
	/**
	 * 只支持office03版本及以下
	 */
	public static String readOfficeContent(File srcFile) throws IOException, Exception{
		String content=null;
		String filePath = srcFile.getAbsolutePath();
		if (filePath.endsWith(".txt")){
			content = FileReaderAll(filePath);
		}else if (filePath.endsWith(".doc")||filePath.endsWith(".docx")) {
			content = WordFileReader(filePath);
		}else if (filePath.endsWith(".xlsx")) {
			content = ExcelXFileReader(filePath);
		}else if (filePath.endsWith(".xls")) {
			content = ExcelFileReader(filePath);
		}else if (filePath.endsWith(".ppt")) {
			content = PptFileReader(filePath);
		}else if (filePath.endsWith(".pptx")) {
			content = PptxFileReader(filePath);
		}else if (filePath.endsWith(".pdf")) {
			content = PdfboxFileReader(filePath);
		}
		return content;
		
	}
	public static String readOfficeContent(String path) throws IOException, Exception{
		String content=null;
		String filePath =path;
		if (filePath.endsWith(".txt")){
			content = FileReaderAll(filePath);
		}else if (filePath.endsWith(".doc")||filePath.endsWith(".docx")) {
			content = WordFileReader(filePath);
		}else if (filePath.endsWith(".xls")||filePath.endsWith(".xlsx")) {
			content = ExcelFileReader(filePath);
		}else if (filePath.endsWith(".ppt")||filePath.endsWith(".pptx")) {
			content = PptFileReader(filePath);
		}else if (filePath.endsWith(".pdf")) {
			content = PdfboxFileReader(filePath);
		}
		return content;
		
	}
	
	
	
	  /** 
     * 把输入流的内容转化成字符串 
     * @param is 
     * @return 
     */  
    public static String readInputStream(InputStream is){  
        try {  
            ByteArrayOutputStream baos=new ByteArrayOutputStream();  
            int length=0;  
            byte[] buffer=new byte[1024];  
            while((length=is.read(buffer))!=-1){  
                baos.write(buffer, 0, length);  
            }  
            is.close();  
            baos.close();  
            //或者用这种方法  
            //byte[] result=baos.toByteArray();  
            //return new String(result);  
            return baos.toString();  
        } catch (Exception e) {  
            e.printStackTrace();  
            return "获取失败";  
        }  
    } 
    
	  /** 
     * 把tmp临时文件读到指定的文件夹
     * @param 
     * @return 
     */  
    public static String readFile(File file,String title){ 
    	String fileContent="";
    	int byteread = 0;
		File oldfile = file;
		if (oldfile.exists()) { // 文件存在时
		InputStream inStream = null;
		FileOutputStream fs = null;
		File basefile=null;
		File dfile=null;
		try {			
			inStream = new FileInputStream(file);
			//basefile=new File("D:/tfiles/");
			basefile=new File(FarmDocFiles.getFileRealPath());
			if(!basefile.exists()){
				basefile.mkdirs();
			}
			dfile=new File(basefile,title);
			
			fs = new FileOutputStream(dfile);
			byte[] buffer = new byte[2048];
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			fileContent=readOfficeContent(dfile);			
				//System.out.println(fileContent);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return fileContent;
    }  
    
    
    
	
	
	public static void main(String[] args) {
		String s="";
		File file=new File("E:/jeeNeon/.metadata/.plugins/org.eclipse.wst.server.core/tmp1/work/Catalina/localhost/wcp/"
				+ "upload_90193bc6_81aa_4b15_a650_b427fd724915_00000000.tmp");
		File sfile=new File("D:/tfiles" );
		if(!sfile.exists()){
		sfile.mkdirs();}
		sfile=new File("D:/tfiles"+"/test.doc" );
		int byteread = 0;
		InputStream inStream = null;
		FileOutputStream fs = null;
		String fileContent="";
			
    try {
			
			inStream = new FileInputStream(file);
			fs = new FileOutputStream(sfile);
			byte[] buffer = new byte[1444];
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			fileContent=LuceneDocUtil.readOfficeContent(sfile.getCanonicalPath());
				System.out.println(fileContent);
		} catch (Exception e) {
				e.printStackTrace();
			} 
		/*try {
			//s=LuceneDocUtil.readOfficeContent(new File("D:/saveFiles/任务.doc"));
			//s=LuceneDocUtil.readOfficeContent(file.getCanonicalPath().replace("tmp", "doc"));
			//s= LuceneDocUtil.readInputStream(new FileInputStream(file));
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println(s);
	}
}