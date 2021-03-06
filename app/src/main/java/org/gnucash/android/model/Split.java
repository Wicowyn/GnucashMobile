package org.gnucash.android.model;


import android.support.annotation.NonNull;

/**
 * A split amount in a transaction.
 * Every transaction is made up of at least two splits (representing a double entry transaction)
 * <p>The split amount is always stored in the database as the absolute value alongside its transaction type of CREDIT/DEBIT<br/>
 * This is independent of the negative values which are shown in the UI (for user convenience).
 * The actual movement of the balance in the account depends on the type of normal balance of the account and the
 * transaction type of the split.</p>
 *
 * @author Ngewi Fet <ngewif@gmail.com>
 */
public class Split extends BaseModel{
    /**
     * Amount value of this split
     */
    private Money mAmount;

    /**
     * Transaction UID which this split belongs to
     */
    private String mTransactionUID = "";

    /**
     * Account UID which this split belongs to
     */
    private String mAccountUID;

    /**
     * The type of this transaction, credit or debit
     */
    private TransactionType mSplitType = TransactionType.CREDIT;

    /**
     * Memo associated with this split
     */
    private String mMemo;

    /**
     * Initialize split with an amount and account
     * @param amount Money amount of this split
     * @param accountUID String UID of transfer account
     */
    public Split(@NonNull Money amount, String accountUID){
        setAmount(amount);
        setAccountUID(accountUID);
        //NOTE: This is a rather simplististic approach to the split type.
        //It typically also depends on the account type of the account. But we do not want to access
        //the database everytime a split is created. So we keep it simple here. Set the type you want explicity.
        mSplitType = amount.isNegative() ? TransactionType.DEBIT : TransactionType.CREDIT;
    }

    /**
     * Clones the <code>sourceSplit</code> to create a new instance with same fields
     * @param sourceSplit Split to be cloned
     * @param generateUID Determines if the clone should have a new UID or should maintain the one from source
     */
    public Split(Split sourceSplit, boolean generateUID){
        this.mMemo          = sourceSplit.mMemo;
        this.mAccountUID    = sourceSplit.mAccountUID;
        this.mSplitType     = sourceSplit.mSplitType;
        this.mTransactionUID = sourceSplit.mTransactionUID;
        this.mAmount        = sourceSplit.mAmount.absolute();

        if (generateUID){
            generateUID();
        } else {
            setUID(sourceSplit.getUID());
        }
    }

    /**
     * Returns the amount of the split
     * @return Money amount of the split
     */
    public Money getAmount() {
        return mAmount;
    }

    /**
     * Sets the amount of the split
     * @param amount Money amount of this split
     */
    public void setAmount(Money amount) {
        this.mAmount = amount;
    }

    /**
     * Returns transaction GUID to which the split belongs
     * @return String GUID of the transaction
     */
    public String getTransactionUID() {
        return mTransactionUID;
    }

    /**
     * Sets the transaction to which the split belongs
     * @param transactionUID GUID of transaction
     */
    public void setTransactionUID(String transactionUID) {
        this.mTransactionUID = transactionUID;
    }

    /**
     * Returns the account GUID of this split
     * @return GUID of the account
     */
    public String getAccountUID() {
        return mAccountUID;
    }

    /**
     * Sets the GUID of the account of this split
     * @param accountUID GUID of account
     */
    public void setAccountUID(String accountUID) {
        this.mAccountUID = accountUID;
    }

    /**
     * Returns the type of the split
     * @return {@link TransactionType} of the split
     */
    public TransactionType getType() {
        return mSplitType;
    }

    /**
     * Sets the type of this split
     * @param splitType Type of the split
     */
    public void setType(TransactionType splitType) {
        this.mSplitType = splitType;
    }

    /**
     * Returns the memo of this split
     * @return String memo of this split
     */
    public String getMemo() {
        return mMemo;
    }

    /**
     * Sets this split memo
     * @param memo String memo of this split
     */
    public void setMemo(String memo) {
        this.mMemo = memo;
    }

    /**
     * Creates a split which is a pair of this instance.
     * A pair split has all the same attributes except that the SplitType is inverted and it belongs
     * to another account.
     * @param accountUID GUID of account
     * @return New split pair of current split
     * @see TransactionType#invert()
     */
    public Split createPair(String accountUID){
        Split pair = new Split(mAmount.absolute(), accountUID);
        pair.setType(mSplitType.invert());
        pair.setMemo(mMemo);
        pair.setTransactionUID(mTransactionUID);

        return pair;
    }

    /**
     * Clones this split and returns an exact copy.
     * @return New instance of a split which is a copy of the current one
     */
    protected Split clone() throws CloneNotSupportedException {
        super.clone();
        Split split = new Split(mAmount, mAccountUID);
        split.setUID(getUID());
        split.setType(mSplitType);
        split.setMemo(mMemo);
        split.setTransactionUID(mTransactionUID);
        return split;
    }

    /**
     * Checks is this <code>other</code> is a pair split of this.
     * <p>Two splits are considered a pair if they have the same amount and opposite split types</p>
     * @param other the other split of the pair to be tested
     * @return whether the two splits are a pair
     */
    public boolean isPairOf(Split other) {
        return mAmount.absolute().equals(other.mAmount.absolute())
                && mSplitType.invert().equals(other.mSplitType);
    }

    @Override
    public String toString() {
        return mSplitType.name() + " of " + mAmount.toString() + " in account: " + mAccountUID;
    }

    /**
     * Returns a string representation of the split which can be parsed again using {@link org.gnucash.android.model.Split#parseSplit(String)}
     * @return the converted CSV string of this split
     */
    public String toCsv(){
        String sep = ";";
        String splitString = mAmount.asString() + sep + mAmount.getCurrency().getCurrencyCode()
                + sep + mAccountUID + sep + mTransactionUID + sep + mSplitType.name();
        if (mMemo != null){
            splitString = splitString + ";" + mMemo;
        }
        return splitString;
    }

    /**
     * Parses a split which is in the format "<amount>;<currency_code>;<account_uid>;<type>;<memo>".
     * The split input string is the same produced by the {@link Split#toCsv()} method
     *
     * @param splitString String containing formatted split
     * @return Split instance parsed from the string
     */
    public static Split parseSplit(String splitString) {
        String[] tokens = splitString.split(";");
        Money amount = new Money(tokens[0], tokens[1]);
        Split split = new Split(amount, tokens[2]);
        split.setTransactionUID(tokens[3]);
        split.setType(TransactionType.valueOf(tokens[4]));
        if (tokens.length == 6){
            split.setMemo(tokens[5]);
        }
        return split;
    }
}
