package com.farm.wcp.controller;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.farm.authority.FarmAuthorityService;
import com.farm.authority.service.UserServiceInter;
import com.farm.core.page.ViewMode;
import com.farm.core.sql.result.DataResult;
import com.farm.doc.domain.ex.DocBrief;
import com.farm.doc.domain.ex.TypeBrief;
import com.farm.doc.server.FarmDocIndexInter;
import com.farm.doc.server.FarmDocManagerInter;
import com.farm.doc.server.FarmDocOperateRightInter;
import com.farm.doc.server.FarmDocRunInfoInter;
import com.farm.doc.server.FarmDocTypeInter;
import com.farm.doc.server.FarmDocgroupManagerInter;
import com.farm.doc.server.FarmDocmessageManagerInter;
import com.farm.doc.server.FarmFileManagerInter;
import com.farm.parameter.FarmParameterService;
import com.farm.util.web.WebHotCase;
import com.farm.wcp.know.service.KnowServiceInter;
import com.farm.wcp.util.ThemesUtil;
import com.farm.web.WebUtils;
import com.farm.web.online.OnlineUserOpImpl;
import com.farm.web.online.OnlineUserOpInter;
import com.fasterxml.jackson.databind.util.JSONPObject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 检索
 * 
 * @author wangdong
 *
 */
@RequestMapping("/websearch")
@Controller
public class DocLuceneController extends WebUtils {
	
	static Logger logger = Logger.getLogger(DocLuceneController.class);
	
	@Resource
	private FarmDocgroupManagerInter farmDocgroupManagerImpl;
	@Resource
	private FarmFileManagerInter farmFileManagerImpl;
	@Resource
	private FarmDocManagerInter farmDocManagerImpl;
	@Resource
	private FarmDocRunInfoInter farmDocRunInfoImpl;
	@Resource
	private KnowServiceInter KnowServiceImpl;
	@Resource
	private FarmDocTypeInter farmDocTypeManagerImpl;
	@Resource
	private FarmDocmessageManagerInter farmDocmessageManagerImpl;
	@Resource
	private FarmDocOperateRightInter farmDocOperateRightImpl;
	@Resource
	private FarmDocIndexInter farmDocIndexManagerImpl;
	@Resource
	private UserServiceInter userServiceImpl;

	/**
	 * 检索首页
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/PubHome", method = RequestMethod.GET)
	public ModelAndView show(Integer pagenum, HttpSession session, HttpServletRequest request) throws Exception {
		ViewMode mode = ViewMode.getInstance();
		List<String> hotCase = WebHotCase.getCases(Integer.valueOf(FarmParameterService.getInstance().getParameter("config.sys.webhotcase.show.num")));
		// 用户分类
		List<TypeBrief> typesons = farmDocTypeManagerImpl.getTypeInfos(getCurrentUser(session), "NONE");
		// 用户小组
		if (getCurrentUser(session) != null) {
			DataResult groups = farmDocgroupManagerImpl.getGroupsByUser(getCurrentUser(session).getId(), 100, 1);
			mode.putAttr("groups", groups.getResultList());
		}
		// 获取前五条置顶文档
		List<DocBrief> topdocs = farmDocRunInfoImpl.getPubTopDoc(2);
		// 加载热词
		List<DocBrief> hotdocs = farmDocRunInfoImpl.getPubHotDoc(10);
		return mode.putAttr("typesons", typesons).putAttr("topDocList", topdocs).putAttr("hotdocs", hotdocs)
				.putAttr("hotCase", hotCase).returnModelAndView(ThemesUtil.getThemePath() + "/lucene/search");
	}

	/**
	 * 检索
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/PubDo")
	public ModelAndView search(String word, Integer pagenum, HttpSession session, HttpServletRequest request)
			throws Exception {
		String userid = null;
		if (getCurrentUser(session) != null) {
			userid = getCurrentUser(session).getId();
		}
		if (word == null) {
			word = "";
		}
		word = URLDecoder.decode(word, "utf-8");
		ViewMode mode = ViewMode.getInstance();
		List<String> hotCase = WebHotCase.getCases(Integer.valueOf(FarmParameterService.getInstance().getParameter("config.sys.webhotcase.show.num")));
		if (word == null || word.isEmpty()) {
			List<TypeBrief> typesons = farmDocTypeManagerImpl.getTypeInfos(getCurrentUser(session), "NONE");
			// 获取前五条置顶文档
			List<DocBrief> topdocs = farmDocRunInfoImpl.getPubTopDoc(2);
			// 加载最热知识
			List<DocBrief> hotdocs = farmDocRunInfoImpl.getPubHotDoc(10);
			return mode.setError("请输入检索词").putAttr("topDocList", topdocs).putAttr("hotCase", hotCase)
					.putAttr("hotdocs", hotdocs).putAttr("typesons", typesons)
					.returnModelAndView(ThemesUtil.getThemePath() + "/lucene/search");
		}
		try {
			List<TypeBrief> types = farmDocTypeManagerImpl.getPopTypesForReadDoc(getCurrentUser(session));
			DataResult result = farmDocIndexManagerImpl.search(word, userid, pagenum);
			return mode.putAttr("result", result).putAttr("types", types).putAttr("hotCase", hotCase)
					.putAttr("word", word).returnModelAndView(ThemesUtil.getThemePath() + "/lucene/searchResult");
		} catch (Exception e) {
			return mode.setError(e.toString()).returnModelAndView(ThemesUtil.getThemePath() + "/error");
		}
	}
	
	/**
	 * 提交同步并登录请求
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping("/webSynSubmit")
	public ModelAndView webSynCommit(String code,HttpServletRequest request, HttpSession session) {
		String goUrl = "";
		try {
			if ("wcp".equals(code)) {
				// 注册session
				loginIntoSession(session, getCurrentIp(request), "sysadmin");
			}else{				
				goUrl = "/websearch/PubHome.html";
			}
			return ViewMode.getInstance().returnRedirectUrl(goUrl);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return ViewMode.getInstance().returnRedirectUrl(goUrl);
	}
	
	/**
	 * 将登陆信息写进session
	 * 
	 * @param session
	 * @param ip
	 * @param loginName
	 */
	private void loginIntoSession(HttpSession session, String ip, String loginName) {
		// 开始写入session用户信息
		setCurrentUser(FarmAuthorityService.getInstance().getUserByLoginName(loginName), session);
		setLoginTime(session);
		// 开始写入session用户权限
		setCurrentUserAction(FarmAuthorityService.getInstance().getUserAuthKeys(getCurrentUser(session).getId()),
				session);
		// 开始写入session用户菜单
		setCurrentUserMenu(FarmAuthorityService.getInstance().getUserMenu(getCurrentUser(session).getId()), session);
		// 写入用户上线信息
		OnlineUserOpInter ouop = null;
		ouop = OnlineUserOpImpl.getInstance(ip, loginName, session);
		ouop.userLoginHandle(FarmAuthorityService.getInstance().getUserByLoginName(loginName));
		// 记录用户登录时间
		FarmAuthorityService.getInstance().loginHandle(getCurrentUser(session).getId());
	}
	
	/**
	 * 检索-提供给派单系统调用
	 * 
	 * @return
	 * @throws Exception
	 */
/*	@RequestMapping(value = "/PubDoWechat2")
	public ModelAndView searchWechat(String word, Integer pagenum, HttpSession session,
			HttpServletRequest request,HttpServletResponse response) throws Exception {
		response.addHeader("Access-Control-Allow-Origin", "*");
		String userid = null;
		if (getCurrentUser(session) != null) {
			userid = getCurrentUser(session).getId();
		}
		if (word == null) {
			word = "";
		}
		word = URLDecoder.decode(word, "utf-8");
		ViewMode mode = ViewMode.getInstance();
		List<String> hotCase = WebHotCase.getCases(Integer.valueOf(FarmParameterService.getInstance().getParameter("config.sys.webhotcase.show.num")));
		try {
			List<TypeBrief> types = farmDocTypeManagerImpl.getPopTypesForReadDoc(getCurrentUser(session));
			DataResult result = farmDocIndexManagerImpl.search(word, userid, pagenum);
			return mode.putAttr("result", result).putAttr("types", types).putAttr("hotCase", hotCase)
					.putAttr("word", word).returnModelAndView(ThemesUtil.getThemePath() + "/lucene/webchatSearchResult");
		} catch (Exception e) {
			return mode.setError(e.toString()).returnModelAndView(ThemesUtil.getThemePath() + "/error");
		}
	}*/
	
// 提供接口，返回问题类型JSON
		@RequestMapping("/PubDoWechat")
		public void searchWechat(Integer pagenum,HttpServletRequest request,HttpServletResponse response)
				throws Exception {
			response.setContentType("text/plain");  
			response.setCharacterEncoding("UTF-8");   
			String callbackFunName=request.getParameter("callbackparam");
			String word=request.getParameter("word");
			word = URLDecoder.decode(word, "utf-8");
			String userid = null;
			if (getCurrentUser(request.getSession()) != null) {
				userid = getCurrentUser(request.getSession()).getId();
			}
			if (word == null) {
				word = "";
			}
			
			  try {
				  DataResult  result = farmDocIndexManagerImpl.search(word, userid, pagenum);
			        response.getWriter().write(callbackFunName + "("+JSONObject.fromObject(result)+")"); //返回jsonp数据
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			  
			
		}

	/**
	 * 查看知识的关联知识
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/PubRelationDocs")
	@ResponseBody
	public Map<String, Object> relationDocs(String docid, HttpSession session,
			HttpServletRequest request) throws Exception {
		ViewMode page = ViewMode.getInstance();
		try {
			List<DocBrief> relationdocs = farmDocIndexManagerImpl.getRelationDocs(docid, 10);
			page.putAttr("RELATIONDOCS", relationdocs);
		} catch (Exception e) {
			return ViewMode.getInstance().setError(e.getMessage()).returnObjMode();
		}
		return page.returnObjMode();
	}
}
