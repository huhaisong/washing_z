package com.example.hu.mediaplayerapk.bean;

import java.util.List;

/**
 * Created by 码农专栏
 * on 2020-06-04.
 */
public class StockBean {
    /**
     * detail : ["1","2","3","4","5","6","7","8","9","10"]
     * stockName : 捷顺科技
     */
    /**
     * 股票名称
     */
    private String stockName;
    private List<Date> detail;
    int isLadyOrMen;  //男女 -1:unknown: 1:lady, 0: men

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public int getIsLadyOrMen() {
        return isLadyOrMen;
    }

    public void setIsLadyOrMen(int isLadyOrMen) {
        this.isLadyOrMen = isLadyOrMen;
    }

    public List<Date> getDetail() {
        return detail;
    }

    public void setDetail(List<Date> detail) {
        this.detail = detail;
    }

   public static class Date {
        private int totalWashing;
        private int totalInterrupt;
        private int totalLongtime;

        public int getTotalWashing() {
            return totalWashing;
        }

        public void setTotalWashing(int totalWashing) {
            this.totalWashing = totalWashing;
        }

        public int getTotalInterrupt() {
            return totalInterrupt;
        }

        public void setTotalInterrupt(int totalInterrupt) {
            this.totalInterrupt = totalInterrupt;
        }

        public int getTotalLongtime() {
            return totalLongtime;
        }

        public void setTotalLongtime(int totalLongtime) {
            this.totalLongtime = totalLongtime;
        }
    }

}
