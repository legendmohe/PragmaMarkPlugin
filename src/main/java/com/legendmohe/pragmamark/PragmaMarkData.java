package com.legendmohe.pragmamark;

class PragmaMarkData {
    String title;
    int lineNum;

    @Override
    public String toString() {
        return "[" + lineNum + "] " + title;
    }
}
