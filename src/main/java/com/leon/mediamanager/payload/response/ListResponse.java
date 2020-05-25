package com.leon.mediamanager.payload.response;

import com.leon.mediamanager.models.ERole;
import com.leon.mediamanager.models.Role;
import com.leon.mediamanager.models.User;

import java.util.List;

public class ListResponse<T> {
    private List<T> list;

    public ListResponse(List<T> list){
        this.list = list;
    }

    public List<T> getList(){
        return this.list;
    }

    public void setList(List<T> list){
        this.list = list;
    }


}
