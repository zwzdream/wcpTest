<%@ page language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="/view/conf/farmtag.tld" prefix="PF"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<base href="<PF:basePath/>" />
<title>检索-<PF:ParameterValue key="config.sys.title" /></title>
<meta name="description"
	content='<PF:ParameterValue key="config.sys.mate.description"/>'>
<meta name="keywords"
	content='<PF:ParameterValue key="config.sys.mate.keywords"/>'>
<meta name="author"
	content='<PF:ParameterValue key="config.sys.mate.author"/>'>
<meta name="robots" content="noindex,nofllow">
<jsp:include page="../atext/include-web.jsp"></jsp:include>
</head>
<body>
	<div class="containerbox">
		<div class="container ">
			
			<!-- /.row -->
			<div class="row">
				<div class="col-sm-9">
					<div class="panel panel-default" style="margin-top: 60px;">
						<div class="panel-body">
							<jsp:include page="commons/includeWebchatSearchResult.jsp"></jsp:include>
						</div>
					</div>
				</div>

			</div>
		</div>
	</div>

</body>
</html>