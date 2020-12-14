package com.facemoji.cut.utils

import com.facemoji.cut.network.entity.AddPreview

/**
 * @author : ydli
 * @time : 2020/11/25 11:29
 * @description :
 */
object AddPreviewUtils{
    /**
     * 判断list是否存在该数据
     * @param beans list
     * @param name 查询字段
     */
    fun contains(beans: MutableList<AddPreview>, name: String): Boolean {
        if (beans.isEmpty()) return false
        for (_add in beans){
            if (_add.addItem == name){
                return true
            }
        }
        return false
    }

    /**
     * 删除list数据
     * @param beans list
     * @param name 删除字段
     */
    fun remove(beans: MutableList<AddPreview>, name: String) {
        if (beans.isEmpty()) return
        val iterator = beans.iterator()
        while(iterator.hasNext()){
            if (iterator.next().addItem == name) {
                iterator.remove()
            }
        }
    }
}