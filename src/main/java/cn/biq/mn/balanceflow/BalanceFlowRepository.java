package cn.biq.mn.balanceflow;

import cn.biq.mn.base.BaseRepository;
import cn.biq.mn.account.Account;
import cn.biq.mn.book.Book;
import cn.biq.mn.payee.Payee;
import cn.biq.mn.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BalanceFlowRepository extends BaseRepository<BalanceFlow>  {

    boolean existsByAccountOrTo(Account account, Account to);

    boolean existsByBook(Book book);

    long countByCreatorAndInsertAtBetween(User creator, long start, long end);

    boolean existsByPayee(Payee payee);

    List<BalanceFlow> findAllByBookOrderByCreateTimeDesc(Book book);

//    自定义查询，用于生成细节
@Query("SELECT DISTINCT bf FROM BalanceFlow bf " +
        "LEFT JOIN FETCH bf.book " +
        "LEFT JOIN FETCH bf.creator " +
        "LEFT JOIN FETCH bf.group " +
        "LEFT JOIN FETCH bf.account " +
        "LEFT JOIN FETCH bf.payee " +
        "LEFT JOIN FETCH bf.to " +
        "LEFT JOIN FETCH bf.tags t " +
        "LEFT JOIN FETCH t.tag " +  // 如果需要Tag的详细信息
        "LEFT JOIN FETCH bf.categories c " +
        "LEFT JOIN FETCH c.category " +  // 如果需要Category的详细信息
        "LEFT JOIN FETCH bf.files " +  // 新增files关联
        "WHERE bf.book = :book ORDER BY bf.createTime DESC")
List<BalanceFlow> findAllByBookWithAssociations(@Param("book") Book book);
}
