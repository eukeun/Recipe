package com.mvc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.sql.DataSource;

import com.mvc.dto.MainDTO;


public class BoardDAO {

	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection conn = null;
	String sql = "";
	public BoardDAO() {
		Context ctx;
		try {
			ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/Oracle");
			conn = ds.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resClose() {
		try {
			if(rs != null && !rs.isClosed()) {rs.close();}
			if(ps != null && !ps.isClosed()) {ps.close();}
			if(conn != null && !conn.isClosed()) {conn.close();}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String write(MainDTO dto) { //영환
		System.out.println("BoardDAO write() 실행");
		String postId = "";
		String sql = "INSERT INTO post(postId,title,categoryId,recipePrice,item,contents,userId)"+
				     "VALUES(postId_seq.NEXTVAL,?,?,?,?,?,?)";
		try {
			ps = conn.prepareStatement(sql,new String[] {"postId"});
			ps.setString(1, dto.getTitle());
			ps.setString(2, dto.getCategoryId());
			ps.setInt(3, dto.getRecipePrice());
			ps.setString(4, dto.getItem());
			ps.setString(5, dto.getContents());
			ps.setString(6, dto.getUserId());
			ps.executeUpdate();
			rs = ps.getGeneratedKeys(); //값만 받아오기
			if(rs.next()) {
				postId = rs.getString(1); // ps에서 String배열의 1번째 값(postId) 가져오기
				System.out.println("작성된 postId:" + postId);			
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return postId;
	}

	public int upHits(String postId) { //영환
		System.out.println("BoardDAO upHits() 실행 : 조회수 올리기");
		int result = 0;
		
		try {
			conn.setAutoCommit(false);
			String sql = "UPDATE post SET hits = hits+1 WHERE postId=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, postId);
			result = ps.executeUpdate();
			System.out.println("조회수 올리기 성공 : "+result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public MainDTO detail(String postId) { //영환
		System.out.println("BoardDAO detail() 실행 : 게시글 상세보기");
		MainDTO dto = null;
		
			String sql = "SELECT p.postId, p.title, p.contents, m.nickName,p.isDel, "+
						"p.recipePrice, p.hits, p.likes, p.postDate,i.imgnewname noThimgnewname, i.imgId noThimgId,  "+
						"p.item, p.userId, r.rankName, c.categoryname, p.categoryId, "+
						"(SELECT imgId FROM image WHERE fieldid =p.postId AND imgfield='post_th') imgId, "+
						"(SELECT imgnewname FROM image WHERE fieldid =p.postId AND imgfield='post_th') imgnewname "+
						"FROM post p, member m, category c, rank r, image i  "+
						"WHERE p.postId = ? AND p.userId = m.userId  "+
						"AND m.rankId = r.rankId AND p.categoryId = c.categoryId  "+
						"AND i.fieldid =p.postId AND i.imgfield='post' ";
			
			try {
				ps = conn.prepareStatement(sql);
				ps.setString(1, postId);
				rs = ps.executeQuery();
				if(rs.next()) {
				dto = new MainDTO();
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setContents(rs.getString("contents"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setPostDate(rs.getDate("postDate"));
				dto.setItem(rs.getString("item"));
				dto.setUserId(rs.getString("userId")); //아이디
				dto.setNickName(rs.getString("nickName"));
				dto.setRankName(rs.getString("rankName"));
				dto.setCategoryName(rs.getString("categoryName"));
				dto.setCategoryId(rs.getString("categoryId"));
				dto.setIsDel(rs.getString("isDel"));
				dto.setTh_imgNewName(rs.getString("imgNewName"));
				dto.setTh_imgid(rs.getString("imgId"));
				dto.setImgNewName(rs.getString("noThimgnewname"));
				dto.setImgid(rs.getString("noThimgId"));
				System.out.println("게시글 상세보기 dto :"+dto);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if(dto == null) {
				String sql1 = "SELECT p.postId, p.title, p.contents, p.recipePrice, p.hits,p.isDel,p.categoryId, "+
						      "p.likes, p.postDate,p.item, p.userId, r.rankName, c.categoryname,m.nickName "+ 
						      "FROM post p, member m, category c, rank r WHERE p.postId = ? AND p.userId = m.userId "+
						      "AND m.rankId = r.rankId AND p.categoryId = c.categoryId";
				
				try {
					ps = conn.prepareStatement(sql1);
					ps.setString(1, postId);
					rs = ps.executeQuery();
					
					if(rs.next()) {
						dto = new MainDTO();
						dto.setPostId(rs.getString("postId"));
						dto.setTitle(rs.getString("title"));
						dto.setContents(rs.getString("contents"));
						dto.setRecipePrice(rs.getInt("recipePrice"));
						dto.setHits(rs.getInt("hits"));
						dto.setLikes(rs.getInt("likes"));
						dto.setPostDate(rs.getDate("postDate"));
						dto.setItem(rs.getString("item"));
						dto.setUserId(rs.getString("userId")); //아이디
						dto.setNickName(rs.getString("nickName"));
						dto.setRankName(rs.getString("rankName"));
						dto.setCategoryName(rs.getString("categoryName"));
						dto.setCategoryId(rs.getString("categoryId"));
						dto.setIsDel(rs.getString("isDel"));
					
						System.out.println("게시글 상세보기 dto :"+dto);
					}
				}catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		return dto;
	}
	
	public void isHits(MainDTO dto) { //영환
		System.out.println("BoardDAO isHits() 실행 : rollback/commit");
		System.out.println("dto :"+dto);
		try {
			if(dto == null) {
				conn.rollback();
				System.out.println("조회수 올리기 실패 롤백");
			} else {
				conn.commit();
				System.out.println("조회수 올리기 커밋 완료");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int postUpdate(MainDTO dto) { //영환
		System.out.println("BoardDAO postUpdate() 실행 : 게시글 수정 ");
		int result = 0;
		String sql="UPDATE post SET title=?, categoryId=?, recipePrice=?, item=?, contents=? WHERE postId=?";
	
		try {
		ps = conn.prepareStatement(sql);
		ps.setString(1, dto.getTitle());
		ps.setString(2, dto.getCategoryId());
		ps.setInt(3, dto.getRecipePrice());
		ps.setString(4, dto.getItem());
		ps.setString(5, dto.getContents());
		ps.setString(6, dto.getPostId());
		result = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public MainDTO del(int postId) { //영환
		System.out.println("BoardDAO del() 실행 : 게시글 삭제여부 변경");
		MainDTO dto = null;
		try {
			String sql = "UPDATE post SET isDel='Y' WHERE postId=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, postId);
			ps.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
		}
		try {
			dto = new MainDTO();
			String sql = "SELECT isDel FROM post WHERE postId=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, postId);
			rs = ps.executeQuery();
			if(rs.next()) {
				dto.setIsDel(rs.getString("isDel"));
				}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return dto;
	}

	public ArrayList<MainDTO> postList(int end, int start) { //영환
		System.out.println("BoardDAO postList() 실행 : 게시글 리스트");
		ArrayList<MainDTO> list = new ArrayList<MainDTO>();
		MainDTO dto = null;
		String sql = "SELECT rnum,postid, title,recipePrice, item, hits, likes, imgNewName, nickname, categoryId "+
					 "FROM(SELECT ROW_NUMBER() OVER(ORDER BY p.postDate DESC)AS RNUM "+
				     ",p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName,p.categoryId "+
				     ",(SELECT nickname FROM member WHERE userid=p.userId) nickname "+
				     "FROM post p LEFT OUTER JOIN image i ON p.postId=i.fieldId AND i.imgField='post_th' "+
				     "WHERE p.isDel = 'N' "+
				     "ORDER BY p.postDate DESC) WHERE rnum between ? AND ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, start);
			ps.setInt(2, end);
			rs = ps.executeQuery();
			while(rs.next()) {
				dto = new MainDTO();
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				String item = rs.getString("item");
				// 재료 3개만 가져오기
	            String[] itemArr = item.split(",");
	            item = "";
	            int i = 0;
	            while (i < 3 && itemArr.length > i) {
	               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
	               item += (",#" + itemArr[i]);
	               i += 1;
	               //System.out.println("i : " + i);
	            }
	            item = item.substring(1);
	            // System.out.println("재료(item)3개만 : "+item);
				dto.setItem(item);
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setImgNewName(rs.getString("imgNewName"));
				dto.setNickName(rs.getString("nickName"));
				dto.setCategoryId(rs.getString("categoryId"));
				list.add(dto);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public HashMap<String, Object> postList() { //영환
		System.out.println("BoardDAO postList() 실행 : 게시글 리스트");
		ArrayList<MainDTO> list = new ArrayList<MainDTO>();
		HashMap<String, Object> map = new HashMap<String, Object>();
		MainDTO dto = null;
		String sql = "SELECT rnum,postid, title,recipePrice, item, hits, likes, imgNewName, nickname, categoryId "+
					 "FROM(SELECT ROW_NUMBER() OVER(ORDER BY p.postDate DESC)AS RNUM "+
				     ",p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName,p.categoryId "+
				     ",(SELECT nickname FROM member WHERE userid=p.userId) nickname "+
				     "FROM post p LEFT OUTER JOIN image i ON p.postId=i.fieldId AND i.imgField='post_th' "+
				     "WHERE p.isDel = 'N' "+
				     "ORDER BY p.postDate DESC) where rnum <= 3";
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()) {
				dto = new MainDTO();
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				String item = rs.getString("item");
				// 재료 3개만 가져오기
	            String[] itemArr = item.split(",");
	            item = "";
	            int i = 0;
	            while (i < 3 && itemArr.length > i) {
	               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
	               item += (",#" + itemArr[i]);
	               i += 1;
	               //System.out.println("i : " + i);
	            }
	            item = item.substring(1);
	            // System.out.println("재료(item)3개만 : "+item);
				dto.setItem(item);
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setImgNewName(rs.getString("imgNewName"));
				dto.setNickName(rs.getString("nickName"));
				dto.setCategoryId(rs.getString("categoryId"));
				list.add(dto);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		map.put("post", list);
		return map;
	}

	public int totalCount() { //영환
		System.out.println("BoardDAO totalCount() 실행");
		String sql = "SELECT COUNT(postId) FROM post WHERE isdel='N' ";
		int total = 0;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			
			
			if(rs.next()) {
				total = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return total;
	}

	public ArrayList<MainDTO> categoryList(int end, int start, int categoryId) { //영환
		System.out.println("BoardDAO categoryList() 실행 : 게시글 리스트");
		ArrayList<MainDTO> list = new ArrayList<MainDTO>();
		MainDTO dto = null;
		String sql = "SELECT rnum,postid, title,recipePrice, item, hits, likes, imgNewName, nickname,categoryId "+
					 "FROM(SELECT ROW_NUMBER() OVER(PARTITION BY categoryId ORDER BY p.postDate DESC)AS RNUM "+
				     ",p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName "+
				     ",(SELECT nickname FROM member WHERE userid=p.userId) nickname,p.categoryId "+
				     "FROM post p LEFT OUTER JOIN image i ON (p.postId=i.fieldId AND i.imgField='post_th') WHERE p.isDel = 'N' "+
				     "ORDER BY p.postDate DESC) WHERE categoryId=? AND rnum between ? AND ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, categoryId);
			ps.setInt(2, start);
			ps.setInt(3, end);
			rs = ps.executeQuery();
			while(rs.next()) {
				dto = new MainDTO();
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				String item = rs.getString("item");
				// 재료 3개만 가져오기
	            String[] itemArr = item.split(",");
	            item = "";
	            int i = 0;
	            while (i < 3 && itemArr.length > i) {
	               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
	               item += (",#" + itemArr[i]);
	               i += 1;
	               //System.out.println("i : " + i);
	            }
	            item = item.substring(1);
	            // System.out.println("재료(item)3개만 : "+item);
				dto.setItem(item);
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setImgNewName(rs.getString("imgNewName"));
				dto.setNickName(rs.getString("nickName"));
				dto.setCategoryId(rs.getString("categoryId"));
				list.add(dto);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public int categoryCount(int categoryId) { //영환
		System.out.println("BoardDAO categoryCount() 실행");
		String sql = "SELECT COUNT(postId) FROM post WHERE categoryId=? AND isdel='N' ";
		int total = 0;
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, categoryId);
			rs = ps.executeQuery();
			
			
			if(rs.next()) {
				total = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return total;
	}

	public ArrayList<MainDTO> postSearch(String keyword, String keywordMin, String keywordMax, int categoryId,
			String postSearchOpt, String keywordNickName, String keywordItem, int start, int end) {
		System.out.println("BoardDAO postSearch() 실행");
		ArrayList<MainDTO> list = new ArrayList<MainDTO>();
		MainDTO dto = null;
		String sql = "";			
		String keywords = '%' + keyword + '%';
		String keywordNickNames = '%' + keywordNickName + '%';
		String keywordItems = '%' + keywordItem + '%';
		
		if(postSearchOpt.equals("title_contentsSearch") && categoryId == 0) {
			System.out.println("categoryId == 0 && postSearchOpt.equals(\"title_contentsSearch\")  실행");
			
			sql =   "SELECT rnum,postid, title,recipePrice, item, hits, likes, imgNewName, nickname,categoryId,contents "+ 
					"FROM(SELECT ROW_NUMBER() OVER(ORDER BY p.postDate DESC)AS RNUM "+ 
					",p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName,p.categoryId,p.contents "+ 
					",(SELECT nickname FROM member WHERE userid=p.userId) nickname "+ 
					"FROM post p LEFT OUTER JOIN image i ON (p.postId=i.fieldId AND i.imgField='post_th') "+ 
					"WHERE p.isDel = 'N' AND (title like ? OR contents like ?) "+
					"ORDER BY p.postDate DESC) WHERE rnum between ? AND ?";
		
			try {
				ps = conn.prepareStatement(sql);
				ps.setString(1, keywords);
				ps.setString(2, keywords);
				ps.setInt(3, start);
				ps.setInt(4, end);
				rs = ps.executeQuery();
				while(rs.next()) {
					dto = new MainDTO();
					dto.setPostId(rs.getString("postId"));
					dto.setTitle(rs.getString("title"));
					dto.setRecipePrice(rs.getInt("recipePrice"));
					String item = rs.getString("item");
					// 재료 3개만 가져오기
		            String[] itemArr = item.split(",");
		            item = "";
		            int i = 0;
		            while (i < 3 && itemArr.length > i) {
		               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
		               item += (",#" + itemArr[i]);
		               i += 1;
		               //System.out.println("i : " + i);
		            }
		            item = item.substring(1);
		            // System.out.println("재료(item)3개만 : "+item);
					dto.setItem(item);
					dto.setHits(rs.getInt("hits"));
					dto.setLikes(rs.getInt("likes"));
					dto.setImgNewName(rs.getString("imgNewName"));
					dto.setNickName(rs.getString("nickName"));
					dto.setCategoryId(rs.getString("categoryId"));
					dto.setContents(rs.getString("contents"));
					list.add(dto);
					}
				} catch (SQLException e) {
				e.printStackTrace();
				}
		}
		
		if(postSearchOpt.equals("title_contentsSearch") && categoryId != 0) {
			System.out.println("title_contentsSearch\") && categoryId != 0  실행");
			
			sql =   "SELECT rnum,postid, title,recipePrice, item, hits, likes, imgNewName, nickname,categoryId,contents "+ 
					"FROM(SELECT ROW_NUMBER() OVER(PARTITION BY categoryId ORDER BY p.postDate DESC)AS RNUM "+ 
					",p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName,p.categoryId,p.contents "+ 
					",(SELECT nickname FROM member WHERE userid=p.userId) nickname "+ 
					"FROM post p LEFT OUTER JOIN image i ON (p.postId=i.fieldId AND i.imgField='post_th') "+ 
					"WHERE p.isDel = 'N' AND (title like ? OR contents like ?) "+
					"ORDER BY p.postDate DESC) WHERE categoryId= ? AND rnum between ? AND ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, keywords);
			ps.setString(2, keywords);
			ps.setInt(3, categoryId);
			ps.setInt(4, start);
			ps.setInt(5, end);
			rs = ps.executeQuery();
			while(rs.next()) {
				dto = new MainDTO();
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				String item = rs.getString("item");
				// 재료 3개만 가져오기
	            String[] itemArr = item.split(",");
	            item = "";
	            int i = 0;
	            while (i < 3 && itemArr.length > i) {
	               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
	               item += (",#" + itemArr[i]);
	               i += 1;
	               //System.out.println("i : " + i);
	            }
	            item = item.substring(1);
	            // System.out.println("재료(item)3개만 : "+item);
				dto.setItem(item);
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setImgNewName(rs.getString("imgNewName"));
				dto.setNickName(rs.getString("nickName"));
				dto.setCategoryId(rs.getString("categoryId"));
				dto.setContents(rs.getString("contents"));
				list.add(dto);
				}
			} catch (SQLException e) {
			e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("recipePriceSearch") && categoryId == 0) {
			System.out.println("categoryId == 0 && postSearchOpt.equals(\"recipePriceSearch\")  실행");
			
			sql =   "SELECT rnum,postid, title,recipePrice, item, hits, likes, imgNewName, nickname,categoryId,contents "+ 
					"FROM(SELECT ROW_NUMBER() OVER(ORDER BY p.postDate DESC)AS RNUM "+ 
					",p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName,p.categoryId,p.contents "+ 
					",(SELECT nickname FROM member WHERE userid=p.userId) nickname "+ 
					"FROM post p LEFT OUTER JOIN image i ON (p.postId=i.fieldId AND i.imgField='post_th') "+ 
					"WHERE  p.isDel = 'N' AND recipePrice between ? AND ? "+
					"ORDER BY p.postDate DESC) WHERE rnum between ? AND ?";
			try {
				ps = conn.prepareStatement(sql);
				ps.setString(1, keywordMin);
				ps.setString(2, keywordMax);
				ps.setInt(3, start);
				ps.setInt(4, end);
				rs = ps.executeQuery();
				while(rs.next()) {
					dto = new MainDTO();
					dto.setPostId(rs.getString("postId"));
					dto.setTitle(rs.getString("title"));
					dto.setRecipePrice(rs.getInt("recipePrice"));
					String item = rs.getString("item");
					// 재료 3개만 가져오기
		            String[] itemArr = item.split(",");
		            item = "";
		            int i = 0;
		            while (i < 3 && itemArr.length > i) {
		               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
		               item += (",#" + itemArr[i]);
		               i += 1;
		               //System.out.println("i : " + i);
		            }
		            item = item.substring(1);
		            // System.out.println("재료(item)3개만 : "+item);
					dto.setItem(item);
					dto.setHits(rs.getInt("hits"));
					dto.setLikes(rs.getInt("likes"));
					dto.setImgNewName(rs.getString("imgNewName"));
					dto.setNickName(rs.getString("nickName"));
					dto.setCategoryId(rs.getString("categoryId"));
					dto.setContents(rs.getString("contents"));
					list.add(dto);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("recipePriceSearch") && categoryId != 0) {
			System.out.println("postSearchOpt.equals(\"recipePriceSearch\") && categoryId != 0  실행");
				
			sql =   "SELECT rnum,postid, title,recipePrice, item, hits, likes, imgNewName, nickname,categoryId,contents "+ 
					"FROM(SELECT ROW_NUMBER() OVER(PARTITION BY categoryId ORDER BY p.postDate DESC)AS RNUM "+ 
					",p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName,p.categoryId,p.contents "+ 
					",(SELECT nickname FROM member WHERE userid=p.userId) nickname "+ 
					"FROM post p LEFT OUTER JOIN image i ON (p.postId=i.fieldId AND i.imgField='post_th') "+ 
					"WHERE p.isDel = 'N' AND recipePrice between ? AND ? "+
					"ORDER BY p.postDate DESC) WHERE categoryId= ? AND rnum between ? AND ?";
			
				try {
					ps = conn.prepareStatement(sql);
					ps.setString(1, keywordMin);
					ps.setString(2, keywordMax);
					ps.setInt(3, categoryId);
					ps.setInt(4, start);
					ps.setInt(5, end);
					rs = ps.executeQuery();
					while(rs.next()) {
						dto = new MainDTO();
						dto.setPostId(rs.getString("postId"));
						dto.setTitle(rs.getString("title"));
						dto.setRecipePrice(rs.getInt("recipePrice"));
						String item = rs.getString("item");
						// 재료 3개만 가져오기
			            String[] itemArr = item.split(",");
			            item = "";
			            int i = 0;
			            while (i < 3 && itemArr.length > i) {
			               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
			               item += (",#" + itemArr[i]);
			               i += 1;
			               //System.out.println("i : " + i);
			            }
			            item = item.substring(1);
			            // System.out.println("재료(item)3개만 : "+item);
						dto.setItem(item);
						dto.setHits(rs.getInt("hits"));
						dto.setLikes(rs.getInt("likes"));
						dto.setImgNewName(rs.getString("imgNewName"));
						dto.setNickName(rs.getString("nickName"));
						dto.setCategoryId(rs.getString("categoryId"));
						dto.setContents(rs.getString("contents"));
						list.add(dto);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		
		if(postSearchOpt.equals("nickNameSearch") && categoryId == 0) {
			System.out.println("categoryId == 0 && postSearchOpt.equals(\"nickNameSearch\")  실행");
			
			sql = "SELECT * FROM(SELECT ROWNUM rnum, u.*  FROM (SELECT postid, title, recipePrice, item, hits, likes, imgNewName, "+
				  "nickname,categoryId,contents FROM(SELECT p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName, "+
				  "p.categoryId,p.contents, (SELECT nickname FROM member WHERE userid=p.userId) nickname "+
				  "FROM post p LEFT OUTER JOIN image i ON (p.postId=i.fieldId AND i.imgField='post_th') WHERE p.isDel = 'N' "+
				  "ORDER BY p.postDate DESC)) "+
				  "u WHERE nickname like ?) WHERE rnum between ? AND ?";
		
			try {
				ps = conn.prepareStatement(sql);
				ps.setString(1, keywordNickNames);
				ps.setInt(2, start);
				ps.setInt(3, end);
				rs = ps.executeQuery();
				while(rs.next()) {
					dto = new MainDTO();
					dto.setPostId(rs.getString("postId"));
					dto.setTitle(rs.getString("title"));
					dto.setRecipePrice(rs.getInt("recipePrice"));
					String item = rs.getString("item");
					// 재료 3개만 가져오기
		            String[] itemArr = item.split(",");
		            item = "";
		            int i = 0;
		            while (i < 3 && itemArr.length > i) {
		               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
		               item += (",#" + itemArr[i]);
		               i += 1;
		               //System.out.println("i : " + i);
		            }
		            item = item.substring(1);
		            // System.out.println("재료(item)3개만 : "+item);
					dto.setItem(item);
					dto.setHits(rs.getInt("hits"));
					dto.setLikes(rs.getInt("likes"));
					dto.setImgNewName(rs.getString("imgNewName"));
					dto.setNickName(rs.getString("nickName"));
					dto.setCategoryId(rs.getString("categoryId"));
					dto.setContents(rs.getString("contents"));
					list.add(dto);
					}
				} catch (SQLException e) {
				e.printStackTrace();
				}
		}
		
		if(postSearchOpt.equals("nickNameSearch") && categoryId != 0) {
				System.out.println("nickNameSearch\") && categoryId != 0  실행");
				
				sql = "SELECT * FROM(SELECT ROWNUM rnum, u.*  FROM (SELECT postid, title, recipePrice, item, hits, likes, imgNewName, "+
						  "nickname,categoryId,contents FROM(SELECT p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName, "+
						  "p.categoryId,p.contents, (SELECT nickname FROM member WHERE userid=p.userId) nickname "+
						  "FROM post p LEFT OUTER JOIN image i ON (p.postId=i.fieldId AND i.imgField='post_th') WHERE p.isDel = 'N' "+
						  " ORDER BY p.postDate DESC)) "+
						  "u WHERE categoryId=? AND nickname like ?) WHERE rnum between ? AND ?";
				try {
					ps = conn.prepareStatement(sql);
					ps.setInt(1, categoryId);
					ps.setString(2, keywordNickNames);
					ps.setInt(3, start);
					ps.setInt(4, end);
					rs = ps.executeQuery();
					while(rs.next()) {
						dto = new MainDTO();
						dto.setPostId(rs.getString("postId"));
						dto.setTitle(rs.getString("title"));
						dto.setRecipePrice(rs.getInt("recipePrice"));
						String item = rs.getString("item");
						// 재료 3개만 가져오기
			            String[] itemArr = item.split(",");
			            item = "";
			            int i = 0;
			            while (i < 3 && itemArr.length > i) {
			               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
			               item += (",#" + itemArr[i]);
			               i += 1;
			               //System.out.println("i : " + i);
			            }
			            item = item.substring(1);
			            // System.out.println("재료(item)3개만 : "+item);
						dto.setItem(item);
						dto.setHits(rs.getInt("hits"));
						dto.setLikes(rs.getInt("likes"));
						dto.setImgNewName(rs.getString("imgNewName"));
						dto.setNickName(rs.getString("nickName"));
						dto.setCategoryId(rs.getString("categoryId"));
						dto.setContents(rs.getString("contents"));
						list.add(dto);
						}
					} catch (SQLException e) {
					e.printStackTrace();
					}
				}
		
		if(postSearchOpt.equals("itemSearch") && categoryId == 0) {
			System.out.println("categoryId == 0 && postSearchOpt.equals(\"recipePriceSearch\")  실행");
			
			sql = "SELECT rnum,postid, title,recipePrice, item, hits, likes, imgNewName, nickname,categoryId,contents "+ 
					"FROM(SELECT ROW_NUMBER() OVER(ORDER BY p.postDate DESC)AS RNUM "+ 
					",p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName,p.categoryId,p.contents "+ 
					",(SELECT nickname FROM member WHERE userid=p.userId) nickname "+ 
					"FROM post p LEFT OUTER JOIN image i ON (p.postId=i.fieldId AND i.imgField='post_th') "+ 
					"WHERE p.isDel = 'N' AND p.item like ? "+
					" ORDER BY p.postDate DESC) WHERE rnum between ? AND ? ";
					
			try {
				ps = conn.prepareStatement(sql);
				ps.setString(1, keywordItems);
				ps.setInt(2, start);
				ps.setInt(3, end);
				rs = ps.executeQuery();
				while(rs.next()) {
					dto = new MainDTO();
					dto.setPostId(rs.getString("postId"));
					dto.setTitle(rs.getString("title"));
					dto.setRecipePrice(rs.getInt("recipePrice"));
					String item = rs.getString("item");
					// 재료 3개만 가져오기
		            String[] itemArr = item.split(",");
		            item = "";
		            int i = 0;
		            while (i < 3 && itemArr.length > i) {
		               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
		               item += (",#" + itemArr[i]);
		               i += 1;
		               //System.out.println("i : " + i);
		            }
		            item = item.substring(1);
		            // System.out.println("재료(item)3개만 : "+item);
					dto.setItem(item);
					dto.setHits(rs.getInt("hits"));
					dto.setLikes(rs.getInt("likes"));
					dto.setImgNewName(rs.getString("imgNewName"));
					dto.setNickName(rs.getString("nickName"));
					dto.setCategoryId(rs.getString("categoryId"));
					dto.setContents(rs.getString("contents"));
					list.add(dto);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("itemSearch") && categoryId != 0) {
			System.out.println("postSearchOpt.equals(\"itemSearch\") && categoryId != 0  실행");
			
			sql = "SELECT rnum,postid, title,recipePrice, item, hits, likes, imgNewName, nickname,categoryId,contents "+ 
				   "FROM(SELECT ROW_NUMBER() OVER(PARTITION BY categoryId ORDER BY p.postDate DESC)AS RNUM "+
					",p.postId,p.title,p.recipePrice,p.item,p.hits,p.likes,i.imgNewName,p.categoryId,p.contents "+ 
					",(SELECT nickname FROM member WHERE userid=p.userId) nickname "+ 
					"FROM post p LEFT OUTER JOIN image i ON (p.postId=i.fieldId AND i.imgField='post_th') WHERE p.isDel = 'N' AND item like ? "+
					"ORDER BY p.postDate DESC) WHERE categoryId=? AND rnum between ? AND ?";
			
						try {
							ps = conn.prepareStatement(sql);
							ps.setString(1, keywordItems);
							ps.setInt(2, categoryId);
							ps.setInt(3, start);
							ps.setInt(4, end);
							rs = ps.executeQuery();
							while(rs.next()) {
								dto = new MainDTO();
								dto.setPostId(rs.getString("postId"));
								dto.setTitle(rs.getString("title"));
								dto.setRecipePrice(rs.getInt("recipePrice"));
								String item = rs.getString("item");
								// 재료 3개만 가져오기
					            String[] itemArr = item.split(",");
					            item = "";
					            int i = 0;
					            while (i < 3 && itemArr.length > i) {
					               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
					               item += (",#" + itemArr[i]);
					               i += 1;
					               //System.out.println("i : " + i);
					            }
					            item = item.substring(1);
					            // System.out.println("재료(item)3개만 : "+item);
								dto.setItem(item);
								dto.setHits(rs.getInt("hits"));
								dto.setLikes(rs.getInt("likes"));
								dto.setImgNewName(rs.getString("imgNewName"));
								dto.setNickName(rs.getString("nickName"));
								dto.setCategoryId(rs.getString("categoryId"));
								dto.setContents(rs.getString("contents"));
								list.add(dto);
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
				}
		
		return list;
	}

	public int searchCount(String keyword, String keywordMin, String keywordMax, int categoryId, String postSearchOpt, String keywordNickName, String keywordItem) {
		System.out.println("BoardDAO searchCount() 실행");
		int total = 0;
		String keywords = '%' + keyword + '%';
		String keywordNickNames = '%' + keywordNickName + '%';
		String keywordItems = '%' + keywordItem + '%';
		
		if(postSearchOpt.equals("title_contentsSearch") && categoryId == 0) {
			try {
				String sql = "SELECT COUNT(postId) FROM post WHERE title like ? OR contents like ?  ";
				ps = conn.prepareStatement(sql);
				ps.setString(1, keywords);
				ps.setString(2, keywords);
				rs = ps.executeQuery();
				
				if(rs.next()) {
					total = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("title_contentsSearch") && categoryId != 0 ) {
			try {
				String sql = "SELECT COUNT(postId) FROM post WHERE categoryId=? AND title like ? OR contents like ? ";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, categoryId);
				ps.setString(2, keywords);
				ps.setString(3, keywords);
				rs = ps.executeQuery();
				
				if(rs.next()) {
					total = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("recipePriceSearch") && categoryId != 0) {
			try {
				String sql = "SELECT COUNT(postId) FROM post WHERE categoryId=? AND recipePrice between ? AND ?";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, categoryId);
				ps.setString(2, keywordMin);
				ps.setString(3, keywordMax);
				rs = ps.executeQuery();

				if(rs.next()) {
					total = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("recipePriceSearch") && categoryId == 0) {
			try {
				String sql = "SELECT COUNT(postId) FROM post WHERE recipePrice between ? AND ? ";
				ps = conn.prepareStatement(sql);
				ps.setString(1, keywordMin);
				ps.setString(2, keywordMax);
				rs = ps.executeQuery();
				
				if(rs.next()) {
					total = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("nickNameSearch") && categoryId == 0) {
			try {
				String sql = "SELECT COUNT(p.postId) FROM post p, member m WHERE p.userId=m.userId AND nickName like ?  ";
				ps = conn.prepareStatement(sql);
				ps.setString(1, keywordNickNames);
				rs = ps.executeQuery();
				
				if(rs.next()) {
					total = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("nickNameSearch") && categoryId != 0 ) {
			try {
				String sql = "SELECT COUNT(p.postId) FROM post p, member m WHERE p.userId=m.userId AND categoryId=? AND nickName like ? ";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, categoryId);
				ps.setString(2, keywordNickNames);
				rs = ps.executeQuery();
				
				if(rs.next()) {
					total = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("itemSearch") && categoryId == 0) {
			try {
				String sql = "SELECT COUNT(postId) FROM post WHERE item like ? ";
				ps = conn.prepareStatement(sql);
				ps.setString(1, keywordItems);
				rs = ps.executeQuery();
				
				if(rs.next()) {
					total = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(postSearchOpt.equals("itemSearch") && categoryId != 0) {
			try {
				String sql = "SELECT COUNT(postId) FROM post WHERE categoryId=? AND item like ?";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, categoryId);
				ps.setString(2, keywordItems);
				rs = ps.executeQuery();

				if(rs.next()) {
					total = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}	
		System.out.println("total : "+total);
		return total;
	}
	
	public int writeComment(String postId, String content, String userId) { // 은홍
		System.out.println("BoardDAO writeComment() 들어옴");
		int success = 0;
		sql = "INSERT INTO postComment(commentid, comment_content, userId, postId) VALUES(postComment_seq.NEXTVAL, ?,?,?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, content);
			ps.setString(2, userId);
			ps.setString(3, postId);
			success = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	public HashMap<String, Object> loadComments(String postId, int page) { // 은홍
		System.out.println("DAO loadComments() 들어옴");
		// 댓글 및 대댓글 최신순으로 해당 페이지에 보여줄 리스트를 로드한다
		// DTO로 가져올 데이터 : 댓글ID, 대댓글ID, 내용, 작성날짜, 작성자아이디, 작성자닉네임, 작성자등급, 부모id, 댓글/대댓글여부,
		// 삭제여부, 블라인드여부
		ArrayList<MainDTO> list = new ArrayList<MainDTO>();
		int start = 0;
		int end = 0;
		int startPage = 0;
		int endPage = 0;
		int pagePerCnt = 10; //한 페이지당 보여줄 댓글 수
		int pagePerPage = 5; //한 페이지당 보여줄 페이지 수
		
		int total = 0;
		try {
			total = getCommentCount(postId);
		} catch (Exception e1) {
			e1.printStackTrace();
		} //총 댓글 수
		int pages = ((int)(total/pagePerCnt))+1; //만들 수 있는 페이지 수
		System.out.println("DAO 총 데이터 수 : "+total+" / 페이지 수 : "+pages);
		
		if(page > pages) {
			page = pages;
		}
		System.out.println("DAO 요청 받은 페이지 : "+page);
		
		end = page*pagePerCnt; //1p:1~10 2p:11~20 3p:21~30
		start = end-(pagePerCnt-1);
		endPage = pagePerPage*((int)((page-1)/pagePerPage)+1);
		startPage = endPage-pagePerPage+1;
		System.out.println("DAO startPage/endPage : "+startPage+"/"+endPage);
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		
		sql = "SELECT * FROM (SELECT ROWNUM rnum, u.* FROM(SELECT * FROM (SELECT * FROM ((SELECT a.*, (SELECT blindId FROM blind WHERE classification='C' AND fieldId=a.commentid) isblind FROM (SELECT * FROM (SELECT commentid, null recomid, comment_content, to_char(comment_date,'yyyy-mm-dd hh24:mi') comment_date, userid,(SELECT nickName FROM member WHERE userId=c.userId) nickName,(SELECT rankName FROM rank WHERE rankId=(SELECT rankId FROM member WHERE userId=c.userId)) rankName, null parentid, isdel, postid, 'comment' lev FROM postcomment c ORDER BY comment_date desc)) a) UNION (SELECT b.*, (SELECT blindId FROM blind WHERE classification='R' AND fieldId=b.recomid) isblind FROM (SELECT * FROM (SELECT null commentid, recomid, comment_content, to_char(comment_date,'yyyy-mm-dd hh24:mi') comment_date, userid, (SELECT nickName FROM member WHERE userId=r.userId) nickName,(SELECT rankName FROM rank WHERE rankId=(SELECT rankId FROM member WHERE userId=r.userId)) rankName, commentid parentid, isdel, (SELECT postid FROM postcomment WHERE commentid=r.commentid) postid, 'recomment' lev FROM recomment r ORDER BY comment_date desc)) b)) WHERE postId = ? ORDER BY comment_date DESC) START WITH lev='comment' CONNECT BY PRIOR commentid=parentid ORDER siblings BY comment_date DESC) u) WHERE rnum BETWEEN ? AND ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, postId);
			ps.setInt(2, start);
			ps.setInt(3, end);
			rs = ps.executeQuery();
			while (rs.next()) {
				String commentId = rs.getString("commentId");
				String recomId = rs.getString("recomId");
				String content = rs.getString("comment_content");
				String date = rs.getString("comment_date");
				String userId = rs.getString("userId");
				String nickName = rs.getString("nickName");
				String rankName = rs.getString("rankName");
				String parentId = rs.getString("parentId");
				String isDel = rs.getString("isDel");
				String lev = rs.getString("lev");
				String isBlind = rs.getString("isBlind");
				// System.out.println("commentId,recomId,content,date,userId,nickName,rankName,parentId,isDel,lev,isBlind");
				// System.out.println(commentId+"/"+recomId+"/"+content+"/"+date+"/"+userId+"/"+nickName+"/"+rankName+"/"+parentId+"/"+isDel+"/"+lev+"/"+isBlind);

				MainDTO dto = new MainDTO();
				dto.setCommentId(commentId);
				dto.setRecomId(recomId);
				dto.setComment_content(content);
				dto.setComment_date(date);
				dto.setUserId(userId);
				dto.setNickName(nickName);
				dto.setRankName(rankName);
				dto.setParentId(parentId);
				dto.setIsDel(isDel);
				dto.setLev(lev);
				dto.setIsBlind(isBlind);
				list.add(dto);
			}
			System.out.println("BoardDAO loadComments() list size : " + list.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("cmtNum", total);
		map.put("list", list);
		map.put("totalPage", pages);
		map.put("currPage", page);
		map.put("start", startPage);
		map.put("end", endPage);
		return map;
	}

	public int getCommentCount(String postId) { // 은홍
		System.out.println("BoardDAO getCommentCount() 들어옴");
		// 해당 게시글에 달린 코멘트 개수를 가져옴
		int cmtNum = 0;
		sql = "SELECT count(*) cmtCount FROM" + "(SELECT * FROM"
				+ "((SELECT commentid, null recomid, postId FROM postcomment)" + "UNION"
				+ "(SELECT null commentid, recomid commentid, (SELECT postid FROM postcomment WHERE commentid=r.commentid) postid FROM recomment r)"
				+ ") WHERE postId=?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, postId);
			rs = ps.executeQuery();
			if (rs.next()) {
				cmtNum = rs.getInt("cmtCount");
				System.out.println("댓글 및 대댓글 개수 : " + cmtNum);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cmtNum;
	}

	public int updateComment(String commentId, String lev, String content) { // 은홍
		int success = 0;
		if (lev.equals("comment")) {
			System.out.println("BoardDAO updateComment() 들어옴 - 댓글 수정");
			sql = "UPDATE postcomment SET comment_content=? WHERE commentid=?";
			try {
				ps = conn.prepareStatement(sql);
				ps.setString(1, content);
				ps.setString(2, commentId);
				success = ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("BoardDAO updateComment() 들어옴 - 대댓글 수정");
			sql = "UPDATE recomment SET comment_content=? WHERE recomid=?";
			try {
				ps = conn.prepareStatement(sql);
				ps.setString(1, content);
				ps.setString(2, commentId);
				success = ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return success;
	}

	public int writeRecomment(String commentId, String content, String userId) {// 은홍
		System.out.println("BoardDAO writeRecomment() 들어옴");
		int success = 0;
		sql = "INSERT INTO recomment(recomid, comment_content, userId, commentId) VALUES(recomment_seq.NEXTVAL, ?,?,?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, content);
			ps.setString(2, userId);
			ps.setString(3, commentId);
			success = ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return success;
	}

	public int deleteComment(String lev, String commentId) { //은홍
		System.out.println("BoardDAO deleteComment() 들어옴");
		int success = 0;
		if (lev.equals("comment")) { //댓글 삭제의 경우
			sql = "UPDATE postcomment SET isDel='Y' WHERE commentId=?";
			try {
				ps = conn.prepareStatement(sql);
				ps.setString(1, commentId);
				success = ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { //대댓글 삭제의 경우
			sql = "UPDATE recomment SET isDel='Y' WHERE recomId=?";
			try {
				ps = conn.prepareStatement(sql);
				ps.setString(1, commentId);
				success = ps.executeUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return success;
	}
	
	public ArrayList<MainDTO> itemListCall(String item) throws SQLException {//지현
		String sql = "SELECT p.productid, p.productname, p.price, i.imgNewName FROM product p LEFT OUTER JOIN image i "
				+ "ON (p.productid = i.fieldid) AND (i.imgField='product_th') WHERE (p.productname LIKE ?) AND (p.stock > 0) AND (p.isdel = 'N')";

		String[] itemArr = item.split(",");
		ArrayList<MainDTO> itemList = new ArrayList<MainDTO>();
		

		for (String items : itemArr) {
			System.out.println(items);
			String keyword = '%' + items + '%';
			ps = conn.prepareStatement(sql);
			ps.setString(1, keyword);
			rs = ps.executeQuery();

			while (rs.next()) {
				MainDTO dto = new MainDTO();
				dto.setProductId(rs.getString("productid"));
				dto.setProductName(rs.getString("productname"));
				dto.setPrice(rs.getInt("price"));
				dto.setImgNewName(rs.getString("imgNewName"));
				itemList.add(dto);
			}
			
		}
		System.out.println("itemList : "+itemList);
		return itemList;
	}
	
	public boolean postLike(String postId, String userId) { //영환
		System.out.println("BoardDAO postLike() 실행");
		boolean success = false;
		int result = 0;
		sql = "INSERT INTO PostLike(postId,userId) VALUES(?,?)";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, postId);
			ps.setString(2, userId);
			result = ps.executeUpdate();
			if(result>0) {
				success = true;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return success;
	}

	public int postLikeCountUp(String postId) { //영환
		System.out.println("BoardDAO postLikeCountUp() 실행");
		int result = 0;
		sql =  "UPDATE post SET likes =likes+1 WHERE postId=?";
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql);
			ps.setString(1, postId);
			result = ps.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void isLike(boolean success) { //영환
		System.out.println("BoardDAO isLike() 실행 : rollback/commit");

		try {
			if(success == false) {
				conn.rollback();
				System.out.println("좋아요 올리기 실패 롤백");
			} else {
				conn.commit();
				System.out.println("좋아요 올리기 커밋 완료");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public int postLikeCheck(String postId, String userId) { //영환
		System.out.println("BoardDAO postLikeCheck() 실행 ");
		int total = 0;
		String sql = "SELECT count(userId) FROM postLike WHERE postid = ? AND userId=?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, postId);
			ps.setString(2, userId);
			rs = ps.executeQuery();
			if(rs.next()) {
				total = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return total;
	
	}
	
	public HashMap<String, Object> bestPost(int page) { // 찬호

		System.out.println("page : " + page);

		ArrayList<MainDTO> bestPost = new ArrayList<MainDTO>();
		String item = "로드실패";
		int start = 0;
		int end = 0;
		int endPage = 0;
		int startPage = 0;
		int pagePerCnt = 9; // 한 페이지당 보여줄 게시글 수
		int pagePerPage = 5; // 한 페이지당 보여줄 페이지 수
		int total = 0;
		try {
			total = bestPostTotalCount();
		} catch (Exception e) {
			e.printStackTrace();
		} // 총 게시글 수
		int pages = total % pagePerCnt == 0 ? total / pagePerCnt : (total / pagePerCnt) + 1; // 만들 수 있는 페이지 수
		System.out.println("total pages : " + total + " / pages : " + pages);

		if (page > pages) {
			page = pages;
		}
		System.out.println("DAO 요청 받은 페이지 : " + page);

		start = ((page - 1) * pagePerCnt) + 1;
		end = page * pagePerCnt; 
		endPage = pagePerPage * ((int) ((page - 1) / pagePerPage) + 1);
		startPage = endPage - pagePerPage + 1;
		System.out.println("DAO startPage/endPage : " + startPage + "/" + endPage);

		HashMap<String, Object> map = new HashMap<String, Object>();

		String sql = "SELECT rnum, postid, title, recipePrice, hits , likes, userid, nickname , item, bestdate "
				+ "FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, p.postid, p.title, p.recipePrice, "
				+ "p.hits , p.likes, p.userid, (SELECT nickname FROM member WHERE userid=p.userId)nickname, p.item, b.bestdate "
				+ "FROM post p left outer join bestpost b on p.postid = b.postid ORDER BY p.likes desc, p.hits desc) "
				+ "where bestdate is not null and rnum between ? and ?";

		bestPost = new ArrayList<MainDTO>();
		System.out.println("DAO bestPost() size : " + bestPost.size());
		try {
			ps = conn.prepareStatement(sql);
			ps.setInt(1, start);
			ps.setInt(2, end);
			rs = ps.executeQuery();
			bestPost = new ArrayList<MainDTO>();
			System.out.println("들어오는지확인");
			while (rs.next()) {
				MainDTO dto = new MainDTO();
				item = rs.getString("item");
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setUserId(rs.getString("userId"));
				dto.setNickname(rs.getString("nickname"));
				// 재료 3개만 가져오기
	            String[] itemArr = item.split(",");
	            item = "";
	            int i = 0;
	            while (i < 3 && itemArr.length > i) {
	               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
	               item += (",#" + itemArr[i]);
	               i += 1;
	               System.out.println("i : " + i);
	            }
	            item = item.substring(1);
	            // System.out.println("재료(item)3개만 : "+item);
				dto.setItem(item);
				System.out.println("dto : " + dto);
				bestPost.add(dto);
			}
			System.out.println("bestPost() size : " + bestPost.size());

			map.put("bestPost", bestPost);
			map.put("totalPage", pages);
			map.put("currPage", page);
			map.put("start", startPage);
			map.put("end", endPage);
		} catch (Exception e) {
			System.out.println("DAO list()에러");
			e.printStackTrace();
		}
		return map;
	}
	
	public HashMap<String, Object> bestPost() { // 찬호

		ArrayList<MainDTO> bestPost = new ArrayList<MainDTO>();
		String item = "로드실패";

		HashMap<String, Object> map = new HashMap<String, Object>();

		String sql = "SELECT rnum, postid, title, recipePrice, hits , likes, userid, item, bestdate "
				+ "FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, p.postid, p.title, p.recipePrice, p.hits , p.likes, p.userid, p.item, b.bestdate "
				+ "FROM post p left outer join bestpost b on p.postid = b.postid ORDER BY p.likes desc, p.hits desc) where rnum <= 3";

		bestPost = new ArrayList<MainDTO>();
		System.out.println("DAO bestPost() size : " + bestPost.size());
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			bestPost = new ArrayList<MainDTO>();
			System.out.println("들어오는지확인");
			while (rs.next()) {
				MainDTO dto = new MainDTO();
				item = rs.getString("item");
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setUserId(rs.getString("userId"));
				// 재료 3개만 가져오기
	            String[] itemArr = item.split(",");
	            item = "";
	            int i = 0;
	            while (i < 3 && itemArr.length > i) {
	               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
	               item += (",#" + itemArr[i]);
	               i += 1;
	               System.out.println("i : " + i);
	            }
	            item = item.substring(1);
	            // System.out.println("재료(item)3개만 : "+item);
				dto.setItem(item);
				System.out.println("dto : " + dto);
				bestPost.add(dto);
			}
			System.out.println("bestPost() size : " + bestPost.size());

			map.put("bestPost", bestPost);
		} catch (Exception e) {
			System.out.println("DAO list()에러");
			e.printStackTrace();
		}
		return map;
	}

	public int bestPostTotalCount() throws SQLException { // 찬호

		String sql = "SELECT count(*) FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, p.postid, p.title, p.recipePrice, p.hits , p.likes, p.userid, (SELECT nickname FROM member WHERE userid=p.userId)nickname, p.item, b.bestdate FROM post p left outer join bestpost b on p.postid = b.postid ORDER BY p.likes desc, p.hits desc)";
		
		
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();

		int total = 0;

		if (rs.next()) {
			total = rs.getInt(1);
		}

		return total;
	}
	
	public HashMap<String, Object> bestMonth(int page) { // 찬호

		System.out.println("page : " + page);

		ArrayList<MainDTO> bestMonth = new ArrayList<MainDTO>();
		String item = "로드실패";
		int start = 0;
		int end = 0;
		int endPage = 0;
		int startPage = 0;
		int pagePerCnt = 9; // 한 페이지당 보여줄 게시글 수
		int pagePerPage = 5; // 한 페이지당 보여줄 페이지 수
		int total = 0;
		try {
			total = bestMonthTotalCount();
		} catch (Exception e) {
			e.printStackTrace();
		} // 총 게시글 수
		int pages = total % pagePerCnt == 0 ? total / pagePerCnt : (total / pagePerCnt) + 1; // 만들 수 있는 페이지 수
		System.out.println("total pages : " + total + " / pages : " + pages);

		if (page > pages) {
			page = pages;
		}
		System.out.println("DAO 요청 받은 페이지 : " + page);

		start = ((page - 1) * pagePerCnt) + 1;
		end = page * pagePerCnt; 
		endPage = pagePerPage * ((int) ((page - 1) / pagePerPage) + 1);
		startPage = endPage - pagePerPage + 1;
		System.out.println("DAO startPage/endPage : " + startPage + "/" + endPage);

		HashMap<String, Object> map = new HashMap<String, Object>();

		String sql = "SELECT rnum, postid, title, recipePrice, hits , likes, userid, item, nickname, bestdate, bestclassification FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, p.postid, p.title, p.recipePrice, p.hits , p.likes, p.userid, (SELECT nickname FROM member WHERE userid=p.userId)nickname, p.item, b.bestdate, b.bestclassification FROM post p left outer join bestpost b on p.postid = b.postid ORDER BY p.likes desc, p.hits desc) where bestclassification = '월간베스트' and rnum between 1 and 9 order by bestdate desc";


		bestMonth = new ArrayList<MainDTO>();
		System.out.println("DAO bestMonth() size : " + bestMonth.size());
		try {
			ps = conn.prepareStatement(sql);
			// ps.setInt(1, start);
			// ps.setInt(2, end);
			rs = ps.executeQuery();
			bestMonth = new ArrayList<MainDTO>();
			System.out.println("들어오는지확인");
			while (rs.next()) {
				MainDTO dto = new MainDTO();
				item = rs.getString("item");
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setUserId(rs.getString("userId"));
				dto.setNickname(rs.getString("nickname"));
				dto.setBestDate(rs.getString("bestDate"));
				dto.setBestClassification(rs.getString("bestclassification"));
				// 재료 3개만 가져오기
	            String[] itemArr = item.split(",");
	            item = "";
	            int i = 0;
	            while (i < 3 && itemArr.length > i) {
	               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
	               item += (",#" + itemArr[i]);
	               i += 1;
	               System.out.println("i : " + i);
	            }
	            item = item.substring(1);
	            // System.out.println("재료(item)3개만 : "+item);
				dto.setItem(item);
				System.out.println("dto : " + dto);
				bestMonth.add(dto);
			}
			System.out.println("bestPost() size : " + bestMonth.size());

			map.put("bestMonth", bestMonth);
			map.put("totalPage", pages);
			map.put("currPage", page);
			map.put("start", startPage);
			map.put("end", endPage);
		} catch (Exception e) {
			System.out.println("DAO list()에러");
			e.printStackTrace();
		}
		return map;
	}

	public int bestMonthTotalCount() throws SQLException { // 찬호

		String sql = "SELECT count(*) FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, p.postid, p.title, p.recipePrice, p.hits , p.likes, p.userid, (SELECT nickname FROM member WHERE userid=p.userId)nickname, p.item, b.bestdate, b.bestclassification FROM post p left outer join bestpost b on p.postid = b.postid ORDER BY p.likes desc, p.hits desc) where bestclassification = '월간베스트' and rnum between 1 and 9 order by bestdate desc";
		
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();

		int total = 0;

		if (rs.next()) {
			total = rs.getInt(1);
		}

		return total;
	}
	
	public HashMap<String, Object> bestWeek(int page) { // 찬호

		System.out.println("page : " + page);

		ArrayList<MainDTO> bestWeek = new ArrayList<MainDTO>();
		String item = "로드실패";
		int start = 0;
		int end = 0;
		int endPage = 0;
		int startPage = 0;
		int pagePerCnt = 9; // 한 페이지당 보여줄 게시글 수
		int pagePerPage = 5; // 한 페이지당 보여줄 페이지 수
		int total = 0;
		try {
			total = bestWeekTotalCount();
		} catch (Exception e) {
			e.printStackTrace();
		} // 총 게시글 수
		int pages = total % pagePerCnt == 0 ? total / pagePerCnt : (total / pagePerCnt) + 1; // 만들 수 있는 페이지 수
		System.out.println("total pages : " + total + " / pages : " + pages);

		if (page > pages) {
			page = pages;
		}
		System.out.println("DAO 요청 받은 페이지 : " + page);

		start = ((page - 1) * pagePerCnt) + 1;
		end = page * pagePerCnt; 
		endPage = pagePerPage * ((int) ((page - 1) / pagePerPage) + 1);
		startPage = endPage - pagePerPage + 1;
		System.out.println("DAO startPage/endPage : " + startPage + "/" + endPage);

		HashMap<String, Object> map = new HashMap<String, Object>();

		String sql = "SELECT rnum, postid, title, recipePrice, hits , likes, userid, item, nickName, bestdate, bestclassification FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, p.postid, p.title, p.recipePrice, p.hits , p.likes, p.userid, (SELECT nickname FROM member WHERE userid=p.userId)nickname, p.item, b.bestdate, b.bestclassification FROM post p left outer join bestpost b on p.postid = b.postid ORDER BY p.likes desc, p.hits desc) where bestclassification = '주간베스트' and rnum between 1 and 9 order by bestdate desc";


		bestWeek = new ArrayList<MainDTO>();
		System.out.println("DAO bestWeek() size : " + bestWeek.size());
		try {
			ps = conn.prepareStatement(sql);
			// ps.setInt(1, start);
			// ps.setInt(2, end);
			rs = ps.executeQuery();
			bestWeek = new ArrayList<MainDTO>();
			System.out.println("들어오는지확인");
			while (rs.next()) {
				MainDTO dto = new MainDTO();
				item = rs.getString("item");
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setUserId(rs.getString("userId"));
				dto.setNickname(rs.getString("nickname"));
				dto.setBestDate(rs.getString("bestdate"));
				dto.setBestClassification(rs.getString("bestclassification"));
				// 재료 3개만 가져오기
	            String[] itemArr = item.split(",");
	            item = "";
	            int i = 0;
	            while (i < 3 && itemArr.length > i) {
	               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
	               item += (",#" + itemArr[i]);
	               i += 1;
	               System.out.println("i : " + i);
	            }
	            item = item.substring(1);
	            // System.out.println("재료(item)3개만 : "+item);
				dto.setItem(item);
				System.out.println("dto : " + dto);
				bestWeek.add(dto);
			}
			System.out.println("bestPost() size : " + bestWeek.size());

			map.put("bestWeek", bestWeek);
			map.put("totalPage", pages);
			map.put("currPage", page);
			map.put("start", startPage);
			map.put("end", endPage);
		} catch (Exception e) {
			System.out.println("DAO list()에러");
			e.printStackTrace();
		}
		return map;
	}

	public int bestWeekTotalCount() throws SQLException { // 찬호

		String sql = "SELECT count(*) "
				+ "FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, post.* "
				+ "FROM post ORDER BY likes desc, hits desc) WHERE rnum BETWEEN 1 AND 9";
		
		ps = conn.prepareStatement(sql);
		rs = ps.executeQuery();
		
		
		
		int total = 0;

		if (rs.next()) {
			total = rs.getInt(1);
		}

		return total;
	}
	
	public HashMap<String, Object> bestSearch(int page, String startDate, String endDate) { // 찬호

		System.out.println("베스트검색 page : " + page);

		ArrayList<MainDTO> bestSearch = new ArrayList<MainDTO>();
		String item = "로드실패";
		int start = 0;
		int end = 0;
		int endPage = 0;
		int startPage = 0;
		int pagePerCnt = 9; // 한 페이지당 보여줄 게시글 수
		int pagePerPage = 5; // 한 페이지당 보여줄 페이지 수
		int total = 0;
		try {
			total = bestSearchTotalCount(startDate, endDate);
		} catch (Exception e) {
			e.printStackTrace();
		} // 총 게시글 수
		int pages = total % pagePerCnt == 0 ? total / pagePerCnt : (total / pagePerCnt) + 1; // 만들 수 있는 페이지 수
		System.out.println("total pages : " + total + " / pages : " + pages);

		if (page > pages) {
			page = pages;
		}
		System.out.println("베스트검색DAO 요청 받은 페이지 : " + page);

		start = ((page - 1) * pagePerCnt) + 1;
		end = page * pagePerCnt; 
		endPage = pagePerPage * ((int) ((page - 1) / pagePerPage) + 1);
		startPage = endPage - pagePerPage + 1;
		System.out.println("베스트검색DAO startPage/endPage : " + startPage + "/" + endPage);

		HashMap<String, Object> map = new HashMap<String, Object>();
		
		

		String sql = "SELECT rnum, postid, title, recipePrice, hits , likes, userid, item, bestdate "
				+ "FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, p.postid, p.title, p.recipePrice, p.hits , p.likes, p.userid, p.item, b.bestdate "
				+ "FROM post p left outer join bestpost b on p.postid = b.postid ORDER BY p.likes desc, p.hits desc) "
				+ "where to_char(bestdate, 'YYYY-MM-DD') between ? and ? and rnum between ? and ?";


		bestSearch = new ArrayList<MainDTO>();
		System.out.println("DAO bestSearch() size : " + bestSearch.size());
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, startDate);
			ps.setString(2, endDate);
			ps.setInt(3, start);
			ps.setInt(4, end);
			System.out.println("startDate : " + startDate);
			System.out.println("endDate : " + endDate);
			rs = ps.executeQuery();
			bestSearch = new ArrayList<MainDTO>();
			System.out.println("들어오는지확인");
			System.out.println("startDate값 : " + startDate);
			System.out.println("endDate값 : " + endDate);

			while (rs.next()) {
				MainDTO dto = new MainDTO();
				dto.setPostId(rs.getString("postId"));
				dto.setTitle(rs.getString("title"));
				dto.setRecipePrice(rs.getInt("recipePrice"));
				dto.setHits(rs.getInt("hits"));
				dto.setLikes(rs.getInt("likes"));
				dto.setUserId(rs.getString("userId"));
				item = rs.getString("item");
				// 재료 3개만 가져오기
	            String[] itemArr = item.split(",");
	            item = "";
	            int i = 0;
	            while (i < 3 && itemArr.length > i) {
	               // 재료가 3개 미만일 경우 itemArr.length < i 에 걸린다
	               item += (",#" + itemArr[i]);
	               i += 1;
	               System.out.println("i : " + i);
	            }
	            item = item.substring(1);
	            // System.out.println("재료(item)3개만 : "+item);
				dto.setItem(item);
				dto.setBestDate(rs.getString("bestDate"));
				System.out.println("dto : " + dto);
				bestSearch.add(dto);
			}
			System.out.println("bestSearch() size : " + bestSearch.size());

			map.put("bestSearch", bestSearch);
			map.put("totalPage", pages);
			map.put("currPage", page);
			map.put("start", startPage);
			map.put("end", endPage);
		} catch (Exception e) {
			System.out.println("DAO list()에러");
			e.printStackTrace();
		}
		return map;
	}
	
	public int bestSearchTotalCount(String startDate, String endDate) throws SQLException { // 찬호
		
		String sql = "SELECT count(*) "
				+ "FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, p.postid, p.title, p.recipePrice, p.hits , p.likes, p.userid, p.item, b.bestdate "
				+ "FROM post p left outer join bestpost b on p.postid = b.postid ORDER BY p.likes desc, p.hits desc) "
				+ "where to_char(bestdate, 'YYYY-MM-DD') between ? and ?";
		
		ps = conn.prepareStatement(sql);
		ps.setString(1, startDate);
		ps.setString(2, endDate);
		
		rs = ps.executeQuery();
		
		
		
		int total = 0;

		if (rs.next()) {
			total = rs.getInt(1);
		}

		return total;
	}
	
	public ArrayList<String> getBestUserId(String bestcls) {
		//베스트게시물로 등록된 포스트의 작성자들의 userId를 가져오는 메소드
		//bestcls에는 주간베스트면 '주간베스트', 월간베스트면 '월간베스트' 넣기
		System.out.println("DAO getBestUserId() 들어옴, bestcls : "+bestcls);
		ArrayList<String> userIdArr = new ArrayList<String>();
		sql = "SELECT p.userId userId FROM (SELECT postId FROM (SELECT ROW_NUMBER() OVER (ORDER BY bestDate DESC) as rnum, postId FROM bestpost WHERE BestClassification='주간베스트') WHERE rnum <= 9) a, post p WHERE a.postId=p.postId";
		try {
			ps = conn.prepareStatement(sql);
			System.out.println("try문 들어옴 ps : "+ps);
			//ps.setString(1, bestcls);
			rs = ps.executeQuery(); //rs.next가 없음
			while(rs.next()) {
				System.out.println("while문 userId : "+rs.getString("userId"));
				userIdArr.add(rs.getString("userId"));
			}
		} catch (Exception e) {
			e.toString();
			System.out.println(e.toString());
		}
		System.out.println("userIdArr size : "+userIdArr.size());
		return userIdArr;
	}
	
	public int bestSelect1() { // 찬호
		
		int result = 0;
		MemberDAO memDao = new MemberDAO();
		ArrayList<String> userArr = new ArrayList<String>();
		String sql = "INSERT INTO BESTPOST (BESTCLASSIFICATION, BESTDATE, POSTID) "
				+ "SELECT '주간베스트' as BESTCLASSIFICATION, sysdate as BESTDATE, postid "
				+ "FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, post.postid "
				+ "FROM post ORDER BY likes desc, hits desc, postdate desc ) "
				+ "WHERE rnum BETWEEN 1 AND 9";
		try {
			ps = conn.prepareStatement(sql);
			result = ps.executeUpdate();
			
			userArr = getBestUserId("주간베스트");
			System.out.println("bestSelect1() userArr size : "+userArr.size());
			for(String user:userArr) {
				System.out.println("주간베스트 선정된 유저 : "+user);
				memDao.getPoint(user, "주간 베스트 선정", 500);				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("DAO bestSelect1 주간 베스트 선정 성공여부 : "+result);
		return result;
		
	}
	
	public int bestSelect2() { // 찬호
		
		int result = 0;
		MemberDAO memDao = new MemberDAO();
		ArrayList<String> userArr = new ArrayList<String>();
		String sql = "INSERT INTO BESTPOST (BESTCLASSIFICATION, BESTDATE, POSTID) "
				+ "SELECT '월간베스트' as BESTCLASSIFICATION, sysdate as BESTDATE, postid "
				+ "FROM (SELECT ROW_NUMBER() OVER (ORDER BY likes desc) AS rnum, post.postid "
				+ "FROM post ORDER BY likes desc, hits desc, postdate desc ) "
				+ "WHERE rnum BETWEEN 1 AND 9";
		try {
			ps = conn.prepareStatement(sql);
			result = ps.executeUpdate();
			
			userArr = getBestUserId2("월간베스트");
			System.out.println("bestSelect2() userArr size : "+userArr.size());
			for(String user:userArr) {
				System.out.println("월간베스트 선정된 유저 : "+user);
				memDao.getPoint(user, "월간 베스트 선정", 1000);				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("DAO bestSelect2 월간 베스트 선정 성공여부 : "+result);
		return result;
		
	}
	
	public ArrayList<String> getBestUserId2(String bestcls) {
		//베스트게시물로 등록된 포스트의 작성자들의 userId를 가져오는 메소드
		//bestcls에는 주간베스트면 '주간베스트', 월간베스트면 '월간베스트' 넣기
		System.out.println("DAO getBestUserId() 들어옴, bestcls : "+bestcls);
		ArrayList<String> userIdArr = new ArrayList<String>();
		sql = "SELECT p.userId userId FROM (SELECT postId FROM (SELECT ROW_NUMBER() OVER (ORDER BY bestDate DESC) as rnum, postId FROM bestpost WHERE BestClassification='월간베스트') WHERE rnum <= 9) a, post p WHERE a.postId=p.postId";
		try {
			ps = conn.prepareStatement(sql);
			System.out.println("try문 들어옴 ps : "+ps);
			//ps.setString(1, bestcls);
			rs = ps.executeQuery(); //rs.next가 없음
			while(rs.next()) {
				System.out.println("while문 userId : "+rs.getString("userId"));
				userIdArr.add(rs.getString("userId"));
			}
		} catch (Exception e) {
			e.toString();
			System.out.println(e.toString());
		}
		System.out.println("userIdArr size : "+userIdArr.size());
		return userIdArr;
	}


	public boolean blindCheck(MainDTO dto) {
		System.out.println("boardDAO blindCheck() : 실행");
		boolean isBlind = false;
		int blind = 0;
		String sql = "SELECT COUNT(postId) FROM post p, blind b WHERE b.classification = 'P' AND  p.postId = b.fieldId AND postId=?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, dto.getPostId());
			rs = ps.executeQuery();
			if(rs.next()) {
				blind = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("blind : "+blind);
		if(blind == 1) {
			isBlind = true;
		}
		return isBlind;
	}

}

