package com.facemoji.cut.utils

/**
 * @author : ydli
 * @time : 2020/11/27 12:21
 * @description : 拆分字符，首字母大小写
 */
object StringUtils {
    /**
     * @param value   源字符串
     * @param diff    需要当成特点的拆分符
     * @param diff2   替换后的重新拼接的字符串
     * @param capital 是否首字母大写 @true 小写->大写，@false 小写->大写
     * @param down    关闭首字符大小写
     * @return
     */
    fun splitCapital(value: String, diff: String, diff2: String?, capital: Boolean,
        down: Boolean? = true): String {
        if (value.isEmpty()) return value
        val stringBuffer = StringBuffer()
        if (value.contains(diff)) {
            val valueArray = value.split(diff.toRegex()).toTypedArray()
            for (i in valueArray.indices) {
                stringBuffer.append(
                    if (down!!)
                        valueArray[i]
                    else
                        upperCase(valueArray[i], capital)
                )
                if (i != valueArray.size - 1) {
                    stringBuffer.append(diff2)
                }
            }
        }
        return stringBuffer.toString()
    }

    /**
     * 先将字符串转为字符数组，然后将数组的第一个元素，即字符串首字母，
     * 进行ASCII 码前移，ASCII 中大写字母从65开始，
     * 小写字母从97开始，所以这里减去32
     *
     * @param str
     * @param capital
     * @return
     */
    private fun upperCase(str: String, capital: Boolean): String {
        val ch = str.toCharArray()
        if (capital){
            //从小写变成大写
            if (ch[0] in 'a'..'z'){
                ch[0] = (ch[0] - 32)
            }
        }else{
            //从大写变成小写
            if (ch[0] in 'A'..'Z') {
               ch[0] = (ch[0] + 32)
            }
        }
        return String(ch)
    }
}