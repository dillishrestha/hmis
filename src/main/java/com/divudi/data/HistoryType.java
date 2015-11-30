
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.divudi.data;

/**
 *
 * @author Buddhika
 */
public enum HistoryType {
    Sale,
    Issue,
    Order,
    GoodReceive,
    Stock,
    ChannelBooking,
    ChannelBalanceReset,
    @Deprecated
    ChannelBookingCancel,
    ChannelDeposit,
    ChannelDepositCancel,
    AgentBalanceUpdateBill,
    CollectingCentreBalanceUpdateBill,
    CollectingCentreDeposit,
    CollectingCentreDepositCancel,
    CollectingCentreBilling,
    MonthlyRecord,   
}
