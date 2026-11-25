package com.viergegroup.swmain.websecurity.filter

import com.viergegroup.swmain.websecurity.common.CorsHelper
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletResponse

@Component
class CorsFilter: Filter {


    @Throws(IOException::class, ServletException::class)
    override   fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val response: HttpServletResponse = CorsHelper.addResponseHeaders(res)
        chain.doFilter(req, response)
    }
   override fun init(filterConfig: FilterConfig?) {}

    override  fun destroy() {}

}