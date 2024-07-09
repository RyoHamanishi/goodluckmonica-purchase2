package jp.co.flm.market.logic;
import java.sql.Connection;

import java.sql.SQLException;
import java.util.ArrayList;

import jp.co.flm.market.common.MarketSystemException;
import jp.co.flm.market.common.MarketBusinessException;
import jp.co.flm.market.dao.ConnectionManager;
import jp.co.flm.market.dao.MemberDAO;
import jp.co.flm.market.dao.OrdersDAO;
import jp.co.flm.market.dao.StockDAO;
import jp.co.flm.market.entity.Member;
import jp.co.flm.market.entity.Orders;
import jp.co.flm.market.entity.Stock;

/**
 * 商品購入の業務クラスです。
 *
 * @author FLM
 * @version 1.0 YYYY/MM/DD
 */

public class PurchaseProductsLogic {
    /**
     * 引数の会員ID、パスワードをもとに会員情報を検索し、取得した会員情報を返却する。
     *
     * @param memberId
     *            会員ID
     * @param password
     *            パスワード
     * @return 会員情報
     * @throws MarketSystemException
     *             本システムのシステム例外
     *  @throws MarketBusinessException
     *             本システムの業務例外
     */
public Member getMember(String memberId,String password)
throws MarketBusinessException, MarketSystemException{
    Connection con = null;
    Member member = null;

    try {
        con = ConnectionManager.getConnection();

        // 会員ID、パスワードにより会員情報を検索する。
        MemberDAO mdao = new MemberDAO(con);
        member = mdao.getMember(memberId, password);

        if (member == null) {
            // 認証に失敗した場合、エラーを発生させる。
            throw new MarketBusinessException("会員IDまたはパスワードが違います。");
        }

    } catch (SQLException e) {
        throw new MarketSystemException("システムエラーです。システム管理者に連絡してください。");
    } finally {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                throw new MarketSystemException("システムエラーです。システム管理者に連絡してください。");
            }
        }
    }
    return member;
}

/**
 * 注文の挿入、在庫の更新、会員ポイントの更新を一括して行う
 *
 * 
 */

public void processOrder(Orders order, String productId,int quantity,String memberId,int point) {
    Connection con = null;
 // トランザクションを開始
    con.setAutoCommit(false); // 自動コミットを無効にする
    try {
        // 1. insertOrder メソッドを実行
        insertOrder(order);

        // 2. selectStockForUpdate メソッドを実行し、現在の在庫数を取得
        Stock currentStock = selectStockForUpdate(productId);

        // 3. 在庫数が注文数より少ない場合はエラー処理
        if (currentStock.getStock() < quantity) {
            throw new SQLException("在庫不足です。");
        }

        // 4. updateStock メソッドを実行して在庫数を更新
        updateStock(productId, quantity);

        // 5. updateMemberPoint メソッドを実行してポイントを更新
        updateMemberPoint(memberId, point);

        // 全ての処理が成功したらコミット
        connection.commit(); // トランザクションをコミット
    } catch (SQLException e) {
        // エラーが発生したらロールバック
        connection.rollback(); // トランザクションをロールバック
        throw e; // 例外を再スロー
    } finally {
        // 自動コミットを元に戻す
        connection.setAutoCommit(true);
    }
}

}
}
