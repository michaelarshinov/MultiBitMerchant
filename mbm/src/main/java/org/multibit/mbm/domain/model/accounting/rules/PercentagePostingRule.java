package org.multibit.mbm.domain.model.accounting.rules;

import org.multibit.mbm.domain.model.accounting.Account;
import org.multibit.mbm.domain.model.accounting.Entry;
import org.multibit.mbm.domain.model.accounting.PostingRule;
import org.multibit.mbm.domain.model.accounting.SingleEntryTransaction;

/**
 * <p>Rule to provide the following to {@link org.multibit.mbm.domain.model.accounting.Account}:</p>
 * <ul>
 * <li>Implementation of percentage posting rule</li>
 * </ul>
 * <p>A PercentagePostingRule places a copy of the source entry into another designated account,
 * after calculating a given percentage of the source entry.
 * The source account is unaware of this copy.</p>
 *
 * @since 0.0.1
 *         
 */
public class PercentagePostingRule<T extends Entry<T>> implements PostingRule<T> {
  private final Account<T> targetAccount;
  private final double percentage;

  /**
   *
   * @param targetAccount The target account (usually a MemoAccount)
   * @param percentage The decimal representation of the percentage (e.g. 0.95 for 95%)
   */
  public PercentagePostingRule(Account<T> targetAccount, double percentage) {
    this.targetAccount = targetAccount;
    this.percentage = percentage;
  }

  @Override
  public void apply(T sourceEntry) {
    // TODO Review this since it is weak
    long targetAmount = (long) (sourceEntry.getAmount() * percentage);
    T targetEntry = sourceEntry.newInstance(targetAccount, targetAmount, sourceEntry.getWhenCreated(), sourceEntry.getWhenBooked());

    // Can only get an Entry into an Account through a Transaction
    SingleEntryTransaction<T> transaction = new SingleEntryTransaction<T>(targetEntry);
    transaction.commit();
  }

  @Override
  public void reverse(T sourceEntry) {
    // TODO Review this since it is weak
    long targetAmount = (long) (sourceEntry.getAmount() * percentage);
    T targetEntry = sourceEntry.newInstance(targetAccount, -targetAmount, sourceEntry.getWhenCreated(), sourceEntry.getWhenBooked());

    // Can only get an Entry into an Account through a Transaction
    SingleEntryTransaction<T> transaction = new SingleEntryTransaction<T>(targetEntry);
    transaction.commit();
  }

}
