package beans;

/**
 * @author: OnlyElena@mail.ru
 * Date: 4/15/16
 * Time: 8:52 PM
 */
public class SberbankRequest {
    Long executoryProcessId;

    String fileName;

    String requestId;
    String userId;
    String requestDate;
    String requestTime;
    String requestType = "1";
    String osbList;

    /**
     * Номер отдела
     */
//    @Column(name = "DEPARTMENT")
    String department;

    /**
     * Старший судебный пристав
     */
//    @Column(name = "DIV_HEAD_NAME")
    String headBailiff;

    /**
     * Судебный пристав
     */
//    @Column(name = "IP_EXEC_PRIST_NAME")
    String bailiff;

    /**
     * Номер исполнительного производства
     */
//    @Column(name = "DOC_NUMBER")
    String execProcNum;

    /**
     * Сумма исполнительного производства
     */
//    @Column(name = "ID_DEBTSUM")
    String summ;

    /**
     * Номер исполнительного документа
     */
//    @Column(name = "ID_DOCNO")
    String execActNum;

    /**
     * Дата исполнительного документа
     */
//    @Column(name = "ID_DOCDATE")
    String execActDate;

    /**
     * Должник
     */
    String debtorFirstName;
    String debtorLastName;
    String debtorSecondName;

    /**
     * Год рождения должника
     */
//    @Column(name = "DBTR_BORN_YEAR")
    String debtorBirthYear;

    /**
     * Дата рождения должника
     */
//    @Column(name = "ID_DBTR_BORN")
    String debtorBirthDate;

    /**
     * Адрес должника
     */
//    @Column(name = "ID_DBTR_ADR")
    String debtorAddres;

    /**
     * Место рождения должника
     */
//    @Column(name = "ID_DBTR_BORNADR")
    String debtorBornAddres;

//    SberbankResponse response;

//    List<SberbankResponse> responses;

    String PasportNum;
    String PasportSer;
    String PasportType;
    String PasportIssued;
    String PasportDate;
    String PasportCod;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getOsbList() {
        return osbList;
    }

    public void setOsbList(String osbList) {
        this.osbList = osbList;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getHeadBailiff() {
        return headBailiff;
    }

    public void setHeadBailiff(String headBailiff) {
        this.headBailiff = headBailiff;
    }

    public String getBailiff() {
        return bailiff;
    }

    public void setBailiff(String bailiff) {
        this.bailiff = bailiff;
    }

    public String getExecProcNum() {
        return execProcNum;
    }

    public void setExecProcNum(String execProcNum) {
        this.execProcNum = execProcNum;
    }

    public String getSumm() {
        return summ;
    }

    public void setSumm(String summ) {
        this.summ = summ;
    }

    public String getExecActNum() {
        return execActNum;
    }

    public void setExecActNum(String execActNum) {
        this.execActNum = execActNum;
    }

    public String getExecActDate() {
        return execActDate;
    }

    public void setExecActDate(String execActDate) {
        this.execActDate = execActDate;
    }

    public String getDebtorFirstName() {
        return debtorFirstName;
    }

    public void setDebtorFirstName(String debtorFirstName) {
        this.debtorFirstName = debtorFirstName;
    }

    public String getDebtorLastName() {
        return debtorLastName;
    }

    public void setDebtorLastName(String debtorLastName) {
        this.debtorLastName = debtorLastName;
    }

    public String getDebtorSecondName() {
        return debtorSecondName;
    }

    public void setDebtorSecondName(String debtorSecondName) {
        this.debtorSecondName = debtorSecondName;
    }

    public String getDebtorBirthYear() {
        return debtorBirthYear;
    }

    public void setDebtorBirthYear(String debtorBirthYear) {
        this.debtorBirthYear = debtorBirthYear;
    }

    public String getDebtorBirthDate() {
        return debtorBirthDate;
    }

    public void setDebtorBirthDate(String debtorBirthDate) {
        this.debtorBirthDate = debtorBirthDate;
    }

    public String getDebtorAddres() {
        return debtorAddres;
    }

    public void setDebtorAddres(String debtorAddres) {
        this.debtorAddres = debtorAddres;
    }

    public String getDebtorBornAddres() {
        return debtorBornAddres;
    }

    public void setDebtorBornAddres(String debtorBornAddres) {
        this.debtorBornAddres = debtorBornAddres;
    }

//    @Deprecated
//    public SberbankResponse getResponse() {
//        return response;
//    }
//
//    @Deprecated
//    public void setResponse(SberbankResponse response) {
//        this.response = response;
//    }
//
//    public List<SberbankResponse> getResponses() {
//        return responses;
//    }
//
//    public void setResponses(List<SberbankResponse> responses) {
//        this.responses = responses;
//    }

    public Long getExecutoryProcessId() {
        return executoryProcessId;
    }

    public void setExecutoryProcessId(Long executoryProcessId) {
        this.executoryProcessId = executoryProcessId;
    }

    public String getPasportNum() {
        return PasportNum;
    }

    public void setPasportNum(String pasportNum) {
        PasportNum = pasportNum;
    }

    public String getPasportSer() {
        return PasportSer;
    }

    public void setPasportSer(String pasportSer) {
        PasportSer = pasportSer;
    }

    public String getPasportType() {
        return PasportType;
    }

    public void setPasportType(String pasportType) {
        PasportType = pasportType;
    }

    public String getPasportIssued() {
        return PasportIssued;
    }

    public void setPasportIssued(String pasportIssued) {
        PasportIssued = pasportIssued;
    }

    public String getPasportDate() {
        return PasportDate;
    }

    public void setPasportDate(String pasportDate) {
        PasportDate = pasportDate;
    }

    public String getPasportCod() {
        return PasportCod;
    }

    public void setPasportCod(String pasportCod) {
        PasportCod = pasportCod;
    }

    @Override
    public String toString() {
        return "SberbankRequest{" +
                "executoryProcessId=" + executoryProcessId +
                ", fileName='" + fileName + '\'' +
                ", requestId='" + requestId + '\'' +
                ", userId='" + userId + '\'' +
                ", requestDate='" + requestDate + '\'' +
                ", requestTime='" + requestTime + '\'' +
                ", requestType='" + requestType + '\'' +
                ", osbList='" + osbList + '\'' +
                ", department='" + department + '\'' +
                ", headBailiff='" + headBailiff + '\'' +
                ", bailiff='" + bailiff + '\'' +
                ", execProcNum='" + execProcNum + '\'' +
                ", summ='" + summ + '\'' +
                ", execActNum='" + execActNum + '\'' +
                ", execActDate='" + execActDate + '\'' +
                ", debtorFirstName='" + debtorFirstName + '\'' +
                ", debtorLastName='" + debtorLastName + '\'' +
                ", debtorSecondName='" + debtorSecondName + '\'' +
                ", debtorBirthYear='" + debtorBirthYear + '\'' +
                ", debtorBirthDate='" + debtorBirthDate + '\'' +
                ", debtorAddres='" + debtorAddres + '\'' +
                ", debtorBornAddres='" + debtorBornAddres + '\'' +
//                ", response=" + response +
                '}';
    }
}
