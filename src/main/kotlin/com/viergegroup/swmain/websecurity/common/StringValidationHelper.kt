package com.viergegroup.swmain.websecurity.common

import java.sql.Date
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

object StringValidationHelper {


    fun validateEmailFormat(email: String?): Boolean {
        var result = false
        val regex = "^(.+)@(.+)$"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(email)
        result = matcher.matches()
        return result
    }


    /**
     * validatePhoneNumber
     * @param phoneNumber
     * @return isValid
     * @user ihuaylupo
     * @since 2018-07-12
     */
    fun validatePhoneNumber(phoneNumber: String?): Boolean {
        var isValid = false
        val pattern = Pattern.compile("\\+\\d{3}\\d{8}")
        val matcher = pattern.matcher(phoneNumber)
        if (matcher.matches()) {
            isValid = true
        }
        return isValid
    }

    /**
     * hasOnlyAlphanumericCharacters - Validates if the text only has alphanumeric characters.
     * @param text
     * @return result - boolean
     * @user ihuaylupo
     * @since 2018-07-19
     */
    fun hasOnlyAlphanumericCharacters(text: String?): Boolean {
        var result = false
        val regex = "^[a-zA-Z0-9- ]+$"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(text)
        result = matcher.matches()
        return result
    }


    @Throws(ParseException::class)
    fun convertStringToDate(date: String?, format: String?): Date? {
        val utilDate = SimpleDateFormat(format).parse(date)
        return Date(utilDate.time)
    }

    fun convertDateToString(date: java.util.Date?, format: String?): String? {
        var dateString: String? = null
        if (null != date) {
            val df: DateFormat = SimpleDateFormat(format)
            dateString = df.format(date)
        }
        return dateString
    }

    fun isValidDate(date: String?, format: String?): Boolean {
        var valid = false
        valid = try {
            val formatter = DateTimeFormatter.ofPattern(format)
            LocalDate.parse(date, formatter)
            true
        } catch (e: Exception) {
            false
        }
        return valid
    }

    fun isStateValid(state: String): Boolean {
        val inputStr: CharSequence = state
        val pattern = Pattern
            .compile("AL|AK|AR|AZ|CA|CO|CT|DC|DE|FL|GA|HI|IA|ID|IL|IN|KS|KY|LA|MA|MD|ME|MI|MN|MO|MS|MT|NC|ND|NE|NH|NJ|NM|NV|NY|OH|OK|OR|PA|RI|SC|SD|TN|TX|UT|VA|VT|WA|WI|WV|WY|al|ak|ar|az|ca|co|ct|dc|de|fl|ga|hi|ia|id|il|in|ks|ky|la|ma|md|me|mi|mn|mo|ms|mt|nc|nd|ne|nh|nj|nm|nv|ny|oh|ok|or|pa|ri|sc|sd|tn|tx|ut|va|vt|wa|wi|wv|wy")
        val matcher = pattern.matcher(inputStr)
        return if (matcher.matches()) {
            true
        } else {
            false
        }
    }

    fun hasSqllInjectionCharacters(text: String): Boolean {
        val inputStr: CharSequence = text
        val pattern = Pattern
            .compile("^[^\\'\\{\\}\\\\\\;\\$]*$")
        val matcher = pattern.matcher(inputStr)
        return if (matcher.matches()) {
            false
        } else {
            true
        }
    }
}