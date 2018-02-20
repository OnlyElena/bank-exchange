package jdbc;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 1:37 PM
 */
public class BeanConnect {

    String beanId;
    String driverClassName;
    String url;
    String username;
    String password;
    String processName;
    String urlPIEV;
    String authentication;


    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getUrlPIEV() {
        return urlPIEV;
    }

    public void setUrlPIEV(String urlPIEV) {
        this.urlPIEV = urlPIEV;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }
}
