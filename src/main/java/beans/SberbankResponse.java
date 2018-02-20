package beans;

/**
 * User: Andrey V. Panov
 * Date: 8/10/11
 * Time: 10:23 AM
 */
public class SberbankResponse {
    int result;
    String requestFileName;
    String requestId;
    String userId;
    String requestType;
    String responseFileName;
    String responseId;
    String requestDate;
    String requestTime;
    String INN;
    String osbName;
    String osbAddress;
    String osbNumber;
    String osbPhoneNumber;
    String osbBIC;
    String debtorAccount;
    String accountCreateDate;
    String accountBalance;
    String accountDescr;
    String accountCurreny;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getRequestFileName() {
        return requestFileName;
    }

    public void setRequestFileName(String requestFileName) {
        this.requestFileName = requestFileName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getResponseFileName() {
        return responseFileName;
    }

    public void setResponseFileName(String responseFileName) {
        this.responseFileName = responseFileName;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
//        if (requestDate != null){
//            requestDate = requestDate.substring(8,10)+"."+requestDate.substring(5,7)+"."+requestDate.substring(0,4);
//        }
        this.requestDate = requestDate;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getINN() {
        return INN;
    }

    public void setINN(String INN) {
        this.INN = INN;
    }

    public String getOsbName() {
        return osbName;
    }

    public void setOsbName(String osbName) {
        this.osbName = osbName;
    }

    public String getOsbAddress() {
        return osbAddress;
    }

    public void setOsbAddress(String osbAddress) {
        this.osbAddress = osbAddress;
    }

    public String getOsbNumber() {
        return osbNumber;
    }

    public void setOsbNumber(String osbNumber) {
        this.osbNumber = osbNumber;
    }

    public String getOsbPhoneNumber() {
        return osbPhoneNumber;
    }

    public void setOsbPhoneNumber(String osbPhoneNumber) {
        this.osbPhoneNumber = osbPhoneNumber;
    }

    public String getOsbBIC() {
        return osbBIC;
    }

    public void setOsbBIC(String osbBIC) {
        this.osbBIC = osbBIC;
    }

    public String getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(String debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public String getAccountCreateDate() {
        return accountCreateDate;
    }

    public void setAccountCreateDate(String accountCreateDate) {
        this.accountCreateDate = accountCreateDate;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
        accountBalance.replace("\n","");
        if (accountBalance == null ||accountBalance.equals("")) accountBalance = "0";
        this.accountBalance = accountBalance;
    }

    public String getAccountDescr() {
        return accountDescr;
    }

    public void setAccountDescr(String accountDescr) {
        this.accountDescr = accountDescr;
    }

    public String getAccountCurreny() {
        return accountCurreny;
    }

    public void setAccountCurreny(String accountCurreny) {
        this.accountCurreny = accountCurreny;
    }

    @Override
    public String toString() {
        return "SberbankResponse{" +
                "result=" + result +
                ", requestFileName='" + requestFileName + '\'' +
                ", requestId='" + requestId + '\'' +
                ", userId='" + userId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", responseFileName='" + responseFileName + '\'' +
                ", responseId='" + responseId + '\'' +
                ", requestDate='" + requestDate + '\'' +
                ", requestTime='" + requestTime + '\'' +
                ", INN='" + INN + '\'' +
                ", osbName='" + osbName + '\'' +
                ", osbAddress='" + osbAddress + '\'' +
                ", osbNumber='" + osbNumber + '\'' +
                ", osbPhoneNumber='" + osbPhoneNumber + '\'' +
                ", osbBIC='" + osbBIC + '\'' +
                ", debtorAccount='" + debtorAccount + '\'' +
                ", accountCreateDate='" + accountCreateDate + '\'' +
                ", accountBalance='" + accountBalance + '\'' +
                ", accountDescr='" + accountDescr + '\'' +
                ", accountCurreny='" + accountCurreny + '\'' +
                '}';
    }

    public String toStringSber() {
        return "| osbnumber=" + osbNumber +
                "| accountCurreny=" + accountCurreny;
    }

}
