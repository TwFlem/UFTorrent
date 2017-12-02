package com.uftorrent.app.TcpSocket;

public class DownloadRates implements Comparable<DownloadRates> {
    public int peerId;
    public double rate;

    public DownloadRates(int pid, double r) {
        peerId = pid;
        rate = r;
    }

    @Override
    public int compareTo(DownloadRates anotherRate) {
        if (anotherRate.getRate() > this.rate) return 1;
        else if (anotherRate.getRate() < this.rate) return -1;
        else return 0;
    }

    public double getRate() {
        return rate;
    }
}
