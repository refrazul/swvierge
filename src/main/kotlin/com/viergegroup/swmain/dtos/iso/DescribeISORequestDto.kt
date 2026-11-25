package com.viergegroup.swmain.dtos.iso

import com.viergegroup.swmain.entities.RequestEntity
import com.viergegroup.swmain.entities.catalogs.HostType


class DescribeISORequestDto: RequestEntity()  {
    var date=""
    var type=""
    var isoASCII=""
}