package com.blog.api.apispring.dto;

import com.blog.api.apispring.dto.users.UserDetailsDto;

public record LoginResponse(String accessToken,
							UserDetailsDto user)
{
}
