package de.unistuttgart.t2.modulith.order;

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.spy;

/**
 * Credits: Abdalkarim Akilan
 * Source: https://gist.github.com/aboodz/669bf3be61f48f1d0f3ec329e3b83719
 */
public class FakeTransactionTemplate extends TransactionTemplate {

    public static FakeTransactionTemplate spied() {
        return spy(new FakeTransactionTemplate());
    }

    @Override
    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        return action.doInTransaction(new SimpleTransactionStatus());
    }
}