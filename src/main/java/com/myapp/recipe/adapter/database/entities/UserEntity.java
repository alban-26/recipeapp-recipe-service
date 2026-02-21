package com.myapp.recipe.adapter.database.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    private Long userId;
    private String userName;
    private String email;

}
