package com.mvc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.mvc.dto.MainDTO;

public class ShopDAO {

	PreparedStatement ps = null;
	ResultSet rs = null;
	Connection conn = null;
	String sql = "";

	public ShopDAO() {
		Context ctx;
		try {
			ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/Oracle");
			conn = ds.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void resClose() {
		try {
			if (rs != null && !rs.isClosed()) {
				rs.close();
				System.out.println("rsClose");
			}
			if (ps != null && !ps.isClosed()) {
				ps.close();
				System.out.println("psClose");
			}

			if (conn != null && !conn.isClosed()) {
				conn.close();
				System.out.println("connClose");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<MainDTO> shopList() { // 의건
		String sql = "SELECT productid, productname, price, stock FROM product WHERE isDel = 'N' ORDER BY productid DESC";
		String sql_shop = "select p.productid productid, p.productname productname, p.price price, p.stock stock, i.imgnewname imgnewname from product p, image i where isdel='N' AND p.productid=i.fieldid AND i.imgfield='product_th'";
		ArrayList<MainDTO> list = null;
		MainDTO dto = null;

		try {
			ps = conn.prepareStatement(sql_shop);
			rs = ps.executeQuery();
			list = new ArrayList<MainDTO>();
			while (rs.next()) {
				dto = new MainDTO();
				dto.setProductId(rs.getString("productid"));
				dto.setProductName(rs.getString("productname"));
				dto.setPrice(rs.getInt("price"));
				dto.setStock(rs.getInt("stock"));
				dto.setImgNewName(rs.getString("imgnewname"));
				list.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public ArrayList<MainDTO> shopSearch(String keyword) { // 의건
		String sql = "select p.productid productid, p.productname productname, p.price price, p.stock stock, i.imgnewname imgnewname from product p, image i where isdel='N' AND p.productid=i.fieldid AND i.imgfield='product_th' AND productname LIKE ?";
		ArrayList<MainDTO> list = null;
		MainDTO dto = null;
		String keywords = '%' + keyword + '%';
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, keywords);
			rs = ps.executeQuery();
			list = new ArrayList<MainDTO>();
			while (rs.next()) {
				dto = new MainDTO();
				dto.setProductId(rs.getString("productid"));
				dto.setProductName(rs.getString("productname"));
				dto.setPrice(rs.getInt("price"));
				dto.setStock(rs.getInt("stock"));
				dto.setImgNewName(rs.getString("imgnewname"));
				list.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public MainDTO shopDetail(String productid) { // 의건
		MainDTO dto = null;
		String sql = "SELECT p.*, i.imgnewname FROM product p, image i WHERE p.productid = ? AND p.productid=i.fieldid AND i.imgfield='product'";

		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, productid);
			rs = ps.executeQuery();

			if (rs.next()) {
				dto = new MainDTO();
				dto.setProductId(rs.getString("productid"));
				dto.setProductName(rs.getString("productname"));
				dto.setPrice(rs.getInt("price"));
				dto.setStock(rs.getInt("stock"));
				dto.setProductDetail(rs.getString("productdetail"));
				dto.setIsDel(rs.getString("isdel"));
				dto.setImgNewName(rs.getString("imgnewname"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}

	public MainDTO cartChk(String pId, String uId) { // 의건
		System.out.println("상품이 이미 장바구니에 있는지 체크");
		MainDTO dto = null;

		// 장바구니에 담긴 수량 체크
		String sql_pCnt = "SELECT productCount FROM cart WHERE productId = ? AND userId = ?";
		// 상품 재고 체크
		String sql_stock = "SELECT stock FROM product WHERE productId = ?";
		try {
			ps = conn.prepareStatement(sql_pCnt);
			ps.setString(1, pId);
			ps.setString(2, uId);
			rs = ps.executeQuery();
			dto = new MainDTO();
			if (rs.next()) {
				dto.setProductCount(rs.getInt("productCount"));
			}

			ps = conn.prepareStatement(sql_stock);
			ps.setString(1, pId);
			rs = ps.executeQuery();
			if (rs.next()) {
				dto.setStock(rs.getInt("stock"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}

	public MainDTO cartChk(String pId) { // 의건
		System.out.println("재고보다 더 많은 수량을 선택하는지 확인");
		MainDTO dto = null;

		// 상품 재고 체크
		String sql = "SELECT stock FROM product WHERE productId = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, pId);
			rs = ps.executeQuery();
			dto = new MainDTO();
			if (rs.next()) {
				dto.setStock(rs.getInt("stock"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}

	public ArrayList<MainDTO> cartList(String uId) { // 의건
		String sql = "select c.productcount productCount, c.totalprice totalPrice, c.productid productId, c.userid userId, c.productName productName, c.price price, p.stock stock from cart c, product p where userid = ? AND c.productid=p.productid";
		String sql_cart = "select c.productcount productCount, c.totalprice totalPrice, c.productid productId, c.userid userId, c.productName productName, c.price price, p.stock stock, i.imgnewname imgnewname,  i.imgfield from cart c, product p LEFT OUTER JOIN image i on p.productid = i.fieldid WHERE i.imgfield='product' AND c.userid=? AND c.productid=p.productid";

		ArrayList<MainDTO> list = null;
		MainDTO dto = null;

		try {
			ps = conn.prepareStatement(sql_cart);
			ps.setString(1, uId);
			rs = ps.executeQuery();
			list = new ArrayList<MainDTO>();
			while (rs.next()) {
				dto = new MainDTO();
				dto.setProductName(rs.getString("productName"));
				dto.setPrice(rs.getInt("price"));
				dto.setProductCount(rs.getInt("productcount"));
				dto.setTotalPrice(rs.getInt("totalprice"));
				dto.setProductId(rs.getString("productId"));
				dto.setUserId(rs.getString("userid"));
				dto.setStock(rs.getInt("stock"));
				dto.setImgNewName(rs.getString("imgnewname"));
				list.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(list.size());
		return list;
	}

	public int cartAdd(String tPrice, String pPrice, String pId, String uId, String pCnt, String pName) { // 의건
		System.out.println("장바구니에 담기");
		// 장바구니 확인
		String sql = "SELECT productId FROM cart WHERE productId = ? AND userId = ?";
		// 장바구니에 같은 상품이 없을 때
		String sql_insert = "INSERT INTO cart VALUES (?,?,?,?,?,?)";
		// 장바구니에 같은 상품이 있을 때
		String sql_update = "UPDATE cart SET totalPrice = totalPrice + ?, productCount = productCount + ? WHERE productId = ? AND userId = ?";

		int success = 0;
		try {
			System.out.println("장바구니에 이미 담겼는지 확인");
			ps = conn.prepareStatement(sql);
			ps.setString(1, pId);
			ps.setString(2, uId);
			rs = ps.executeQuery();
			if (rs.next()) {
				System.out.println("이미 담겨 있으면 수량 수정");
				ps = conn.prepareStatement(sql_update);
				ps.setString(1, tPrice);
				ps.setString(2, pCnt);
				ps.setString(3, pId);
				ps.setString(4, uId);
				success = ps.executeUpdate();
			} else {
				System.out.println("담겨있지 않으면 장바구니에 추가");
				ps = conn.prepareStatement(sql_insert);
				ps.setString(1, tPrice);
				ps.setString(2, pId);
				ps.setString(3, uId);
				ps.setString(4, pCnt);
				ps.setString(5, pName);
				ps.setString(6, pPrice);
				success = ps.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}

	public int cartModify(String pId, String pCnt, String uId) { // 의건
		String sql = "SELECT price FROM product WHERE productId = ?";
		String sql_update = "UPDATE cart SET totalPrice = ?, productCount = ? WHERE productId = ? AND userId = ?";

		MainDTO dto = new MainDTO();
		int success = 0;
		int pPrice = 0;
		int tPrice = 0;
		String s_tPrice = "";
		try {
			System.out.println("장바구니에서 수량 수정");
			ps = conn.prepareStatement(sql);
			ps.setString(1, pId);
			rs = ps.executeQuery();
			if (rs.next()) {
				dto.setPrice(rs.getInt("price"));
				pPrice = dto.getPrice();
				tPrice = Integer.parseInt(pCnt) * pPrice;
				s_tPrice = Integer.toString(tPrice);

				ps = conn.prepareStatement(sql_update);
				ps.setString(1, s_tPrice);
				ps.setString(2, pCnt);
				ps.setString(3, pId);
				ps.setString(4, uId);
				success = ps.executeUpdate();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}

	public int cartDel(String uId, String[] delList) throws SQLException { // 의건
		String sql = "DELETE FROM cart WHERE productid = ? AND userId = ?";
		int cnt = 0;
		for (String productId : delList) {
			ps = conn.prepareStatement(sql);
			System.out.println("pId/uId : " + productId + "/" + uId);
			ps.setString(1, productId);
			ps.setString(2, uId);
			cnt += ps.executeUpdate();
		}
		return cnt;
	}

	public ArrayList<MainDTO> paymentList(String uId, String[] orderList) { // 의건
		// 장바구니에서 선택해서 주문
		String sql = "SELECT productcount, totalprice, productId, userid, productname, price FROM cart WHERE productid = ? AND userId = ?";
		String sql_payment = "select c.productcount productCount, c.totalprice totalPrice, c.productid productId, c.userid userId, c.productName productName, c.price price, i.imgnewname imgnewname from cart c, product p, image i WHERE p.productid =? AND c.userid=? AND c.productid=p.productid AND p.productid = i.fieldid AND i.imgfield='product_th'";
		ArrayList<MainDTO> list = new ArrayList<MainDTO>();
		MainDTO dto = null;

		for (String productId : orderList) {
			try {
				ps = conn.prepareStatement(sql_payment);
				ps.setString(1, productId);
				ps.setString(2, uId);
				rs = ps.executeQuery();
				while (rs.next()) {
					dto = new MainDTO();
					dto.setProductCount(rs.getInt("productcount"));
					dto.setTotalPrice(rs.getInt("totalprice"));
					dto.setProductId(rs.getString("productId"));
					dto.setPrice(rs.getInt("price"));
					dto.setUserId(rs.getString("userid"));
					dto.setProductName(rs.getString("productname"));
					dto.setImgNewName(rs.getString("imgnewname"));
					System.out.println(dto.getProductName());
					list.add(dto);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		System.out.println(list.size());
		return list;
	}

	public MainDTO memberDetail(String uId) { // 의건
		MainDTO dto = null;
		
		String sql = "SELECT m.name name, m.address address , m.tel tel, r.discount discount FROM rank r, member m WHERE r.rankid = m.rankid and m.userid = ?";
		MemberDAO memDao = new MemberDAO();
		int currCash = memDao.showCash(uId);
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, uId);
			rs = ps.executeQuery();

			if (rs.next()) {
				dto = new MainDTO();
				dto.setName(rs.getString("name"));
				dto.setAddress(rs.getString("address"));
				dto.setTel(rs.getString("tel"));
				dto.setDiscount(rs.getInt("discount"));
				dto.setCash(currCash);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}

	public String payment(String uId, int resultPrice, int orderPrice, int discount) { // 의건
		String sql_insert = "INSERT INTO payment(paymentId, orderPrice, paymentPrice, userId, discount) VALUES (paymentId_seq.NEXTVAL,?,?,?,?)";
		// payment에 주문한 상품 추가

		String pk = "";

		try {
			ps = conn.prepareStatement(sql_insert, new String[] { "paymentId" });
			ps.setInt(1, orderPrice);
			ps.setInt(2, resultPrice);
			ps.setString(3, uId);
			ps.setInt(4, discount);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();

			if (rs.next()) {
				pk = rs.getString(1);
			}

		} catch (

		SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pk;
	}

	public ArrayList<MainDTO> payCart(String uId, String[] productList, String[] stockList, String paymentId) { // 의건

		// String sql_order = "INSERT INTO orderHistory (productName, productCnt, price,
		// paymentId, productId) VALUES (?,?,?,?,?)";

		String sql = "SELECT productName, price FROM product WHERE productId = ?";
		String sql_order = "INSERT INTO orderHistory (productName, price, productCnt, paymentId, productId) VALUES (?,?,?,?,?)";
		String sql_del = "DELETE FROM cart WHERE userId = ? AND productId = ? AND productCount = ?";
		String sql_product = "UPDATE product SET stock = stock - ? WHERE productId = ?";

		ArrayList<MainDTO> list = new ArrayList<MainDTO>();
		MainDTO dto = null;
		int del = 0;
		int cnt = 0;
		int stock = 0;
		int pListLen = productList.length;
		int sListLen = stockList.length;
		System.out.println();
		if (pListLen == sListLen) {
			for (int i = 0; i < pListLen; i++) {
				try {
					// String sql = "SELECT productName, price FROM product WHERE productId = ?";
					ps = conn.prepareStatement(sql); // product 테이블에서 name, price 가져오기
					ps.setString(1, productList[i]);
					System.out.println("productId : " + productList[i]);
					rs = ps.executeQuery();

					if (rs.next()) { // 가져온 정보를
						dto = new MainDTO();
						dto.setProductName(rs.getString("productName"));
						dto.setPrice(rs.getInt("price"));

						list.add(dto);

						// String sql_order = "INSERT INTO orderHistory (productName, price,
						// productCount, paymentId, productId) VALUES (?,?,?,?,?)";
						ps = conn.prepareStatement(sql_order); // 주문한 상품들 정보 orderHistory(주문내역)에 추가
						ps.setString(1, dto.getProductName());
						ps.setInt(2, dto.getPrice());
						ps.setInt(3, Integer.parseInt(stockList[i]));
						ps.setString(4, paymentId);
						ps.setString(5, productList[i]);
						cnt += ps.executeUpdate();
						System.out.println("productId/productCount " + productList[i] + "/" + stockList[i]);

						// String sql_product = "UPDATE product SET stock = stock - ? WHERE productId =
						// ?";
						ps = conn.prepareStatement(sql_product);
						ps.setInt(1, Integer.parseInt(stockList[i]));
						ps.setString(2, productList[i]);
						System.out.println("productCount/productId " + "/" + stockList[i] + productList[i]);
						stock += ps.executeUpdate();

						// String sql_del = "DELETE FROM cart WHERE userId = ? AND productId = ? AND
						// productCount = ?";
						ps = conn.prepareStatement(sql_del);
						ps.setString(1, uId);
						ps.setString(2, productList[i]);
						ps.setInt(3, Integer.parseInt(stockList[i]));
						del += ps.executeUpdate();
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // 주문내역에 넣을 상품명, 상품가격
			}

		}
		System.out.println("주문내역 추가_" + cnt + "/" + "상품재고수정_" + stock + "/" + "장바구니에서 시켰으면 삭제" + del);

		return list;
	}

	public boolean productChk(String[] productList, String[] countList) {
		// String sql_order = "INSERT INTO orderHistory (productName, productCnt, price,
		// paymentId, productId) VALUES (?,?,?,?,?)";

		String sql = "SELECT stock FROM product WHERE productId = ?";
		

		boolean success = false;
		ArrayList<MainDTO> list = new ArrayList<MainDTO>();
		MainDTO dto = null;

		int pListLen = productList.length;
		int sListLen = countList.length;
		System.out.println();
		if (pListLen == sListLen) {
			for (int i = 0; i < pListLen; i++) {
				try {
					// String sql = "SELECT productName, price FROM product WHERE productId = ?";
					ps = conn.prepareStatement(sql); // product 테이블에서 name, price 가져오기
					ps.setString(1, productList[i]);
					System.out.println("productId : " + productList[i]);
					rs = ps.executeQuery();

					if (rs.next()) { // 가져온 정보를
						dto = new MainDTO();
						dto.setStock(rs.getInt("stock"));

						if (Integer.parseInt(countList[i]) <= dto.getStock()) {
							success = true;
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		}
		return success;
	}

	public boolean cashChk(String uId, int resultPrice) {
		MainDTO dto = null;
		boolean success = false;
		String sql = "select total from (select total from cash WHERE userid=? order by changedtime desc) where rownum = 1";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, uId);
			rs = ps.executeQuery();
			if(rs.next()) {
				dto = new MainDTO();
				dto.setTotalPoint(rs.getInt("total"));
				if(resultPrice <= dto.getTotalPoint()) {
					success = true;
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // product 테이블에서 name, price 가져오기
		return success;
		
	}

}
