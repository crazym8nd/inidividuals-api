package com.bnm.individuals_api.mapper;

import com.bnm.individuals_api.dto.AboutMeResponse;
import com.bnm.individuals_api.model.UserData;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    
    AboutMeResponse toAboutMeResponse(UserData userData);
} 