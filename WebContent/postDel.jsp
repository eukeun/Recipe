<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<style>
.postDel{
	 margin: 0 auto 10px;
	 width: 350px;
	 
}

</style>
</head>
<header>
         <c:import url="./header_afterLogin.jsp" />
      </header>
<body>

<div class="postDel">삭제된 게시물 입니다.</div>
<div class="postDel"><input type="button" name="return" onclick="location.href='./postList'" value="전체리스트"/></div>

</body>
</html>