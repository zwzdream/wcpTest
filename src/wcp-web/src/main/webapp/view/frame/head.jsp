<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="/view/conf/farmtag.tld" prefix="PF"%>
<%@ taglib uri="/view/conf/farmdoc.tld" prefix="DOC"%>
<!--40 <span style="font-size: 14px; font-weight: bold;margin: 20px;"> 系统管理控制台2.0.0</span> -->
<div style="background:#1890FF;height: 60px;">
	<table width="100%">
		<tr>
			<td style="width: 247px;"><a
				href='<DOC:defaultIndexPage/>'> <img
					alt="<PF:ParameterValue key="config.sys.title" />"
					src="text/img/LOGO2.png"
					style="display:inline-block;float:left;width:100%;height: 60px" />
			</a></td>
			<td class="head_logo1">&nbsp;</td>
			<td style="float: right; width: 450px; padding-top: 15px; color: #fff;">
				<em class="top_right3"> 
					<%-- <ul class="nav navbar-nav " style="margin-right: 10px;">
						<c:if test="${USEROBJ==null}">
							<li class="active"><a><span
									class=""></span>&nbsp;</a></li>
						</c:if>
						 <c:if test="${USEROBJ!=null}">
							<li class="active"><a><span
									class="glyphicon glyphicon-user"></span>&nbsp;${USEROBJ.name}</a></li>
							<!-- <li class="active"><a href="login/webout.html"><span
									class="glyphicon glyphicon-off"></span>&nbsp;注销</a></li> -->
						</c:if> 
					</ul> --%>
					
					<span class="spanIcon9"><a id="logout_a">回到主页面</a></span> 
					<span class="spanIcon11"><a id="logout_a">系统管理员</a></span> 
					<!-- <span class="spanIcon10"><a id="editPW_a" target="main">修改密码</a></span> -->
				</em>
			</td>
		</tr>
	</table>
</div>
<div id="DIV_EDIT_PASSWORD_WINDOW"></div>
<script type="text/javascript">
	var lastNum;
	$('#logout_a').bind('click', function() {
		$.messager.confirm('显示主页面', '确定要回到主页面吗？', function(r) {
			if (r) {
				/* window.location = basePath + "login/out.do"; */
				window.location = basePath + "login/webout.html";
			}
		});
	});
	$('#editPW_a').bind('click', function() {
		$('#DIV_EDIT_PASSWORD_WINDOW').dialog({
			title : '修改用户密码',
			width : 300,
			height : 200,
			closed : false,
			cache : false,
			href : 'user/updatePassword.do',
			modal : true
		});
	});
</script>