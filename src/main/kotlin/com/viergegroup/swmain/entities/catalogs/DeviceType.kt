package com.viergegroup.swmain.entities.catalogs

import com.viergegroup.swmain.dtos.transactions.TransactionDto
import com.viergegroup.swmain.entities.transactions.TransactionData
import com.viergegroup.swmain.webwatcher.Watcher
import com.viergegroup.swmain.webwatcher.WatcherGlobalLog
/*

-- Elimina la columna si ya existe
ALTER TABLE public.transaction_data
DROP COLUMN IF EXISTS device_type;

-- La crea de nuevo con default 'POS'
ALTER TABLE public.transaction_data
ADD COLUMN device_type VARCHAR(255) DEFAULT 'POS';
 

ALTER TABLE public.transaction_data
ADD COLUMN client_id VARCHAR(255) DEFAULT 'VIERGE';

 
ALTER TABLE public.transaction_data
ADD COLUMN system_trace_audit_number VARCHAR(20) DEFAULT 'VIERGE';


ALTER TABLE public.transaction_data
DROP COLUMN device_type;

ALTER TABLE public.transaction_data
DROP COLUMN client_id;




*/
enum class DeviceType(var deviceTypeId:Int,var deviceTypeName:String){
    Undefined(-1,"Undefined"), 
    MobilePOS(1,"MobilePOS"),
    POS(2,"POS"),  
    PinPad(3,"PinPad"),
    MPos(4,"MPos") 

    ;

}