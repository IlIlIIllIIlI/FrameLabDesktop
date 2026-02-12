package com.frameLab.frameSprite.utils.cookies;

import java.net.HttpCookie;

public class CookieParsing {
    public  String name;
    public String value;
    public String comment;
    public String commentURL;
    public boolean toDiscard;
    public String domain;
    public long maxAge;
    public String path;
    public String portlist;
    public boolean secure;
    public boolean httpOnly;
    public int version;

    CookieParsing(){

    }

    public CookieParsing(HttpCookie cookie){
        this.name = cookie.getName();
        this.value = cookie.getValue();
        this.comment = cookie.getComment();
        this.commentURL = getCommentURL();
        this.toDiscard = cookie.getDiscard();
        this.domain = cookie.getDomain();
        this.maxAge = cookie.getMaxAge();
        this.path = cookie.getPath();
        this.portlist = cookie.getPortlist();
        this.secure = cookie.getSecure();
        this.httpOnly = cookie.isHttpOnly();
        this.version = cookie.getVersion();
    }

    public HttpCookie toCookie(){
        HttpCookie cookie = new HttpCookie(name,value);
        cookie.setComment(comment);
        cookie.setCommentURL(commentURL);
        cookie.setDiscard(toDiscard);
        cookie.setDomain(domain);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        cookie.setPortlist(portlist);
        cookie.setSecure(secure);
        cookie.setHttpOnly(httpOnly);
        cookie.setVersion(version);

        return cookie;

    }
    public String getName() {
        return name;
    }


    public String getValue() {
        return value;
    }

    public String getComment() {
        return comment;
    }

    public boolean isToDiscard() {
        return toDiscard;
    }

    public String getDomain() {
        return domain;
    }

    public String getCommentURL() {
        return commentURL;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public String getPath() {
        return path;
    }

    public String getPortlist() {
        return portlist;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public int getVersion() {
        return version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setPortlist(String portlist) {
        this.portlist = portlist;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setToDiscard(boolean toDiscard) {
        this.toDiscard = toDiscard;
    }

    public void setCommentURL(String commentURL) {
        this.commentURL = commentURL;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
