package cn.bingod.antipyretic;

final class UriTable {

    String uri;
    String className;
    boolean isActivity;

    UriTable(String uri, String className, boolean isActivity) {
        this.className = className;
        this.uri = uri;
        this.isActivity = isActivity;
    }
}
