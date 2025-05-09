package com.pse.tixclick.config;

import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.payload.entity.company.*;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import com.pse.tixclick.payload.response.EventActivityResponse;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.ModelMapper;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Ánh xạ Event -> EventDTO
        modelMapper.addMappings(new PropertyMap<Event, EventDTO>() {
            @Override
            protected void configure() {
                map().setCompanyId(source.getCompany().getCompanyId());
                map().setOrganizerId(source.getOrganizer().getAccountId());
                map().setCategoryId(source.getCategory().getEventCategoryId());
            }
        });

        // Ánh xạ EventActivity -> EventActivityDTO
        modelMapper.addMappings(new PropertyMap<EventActivity, EventActivityDTO>() {
            @Override
            protected void configure() {
                map().setEventId(source.getEvent().getEventId());
                map().setCreatedBy(source.getCreatedBy().getAccountId());
            }
        });

        // Ánh xạ Ticket -> TicketDTO
        modelMapper.addMappings(new PropertyMap<Ticket, TicketDTO>() {
            @Override
            protected void configure() {
                map().setAccountId(source.getAccount().getAccountId());
                map().setEventId(source.getEvent().getEventId());
            }
        });

        // Ánh xạ CompanyDocuments -> CompanyDocumentDTO
        modelMapper.addMappings(new PropertyMap<CompanyDocuments, CompanyDocumentDTO>() {
            @Override
            protected void configure() {
                map().setCompanyId(source.getCompany().getCompanyId());
            }
        });

        // Ánh xạ CompanyVerification -> CompanyVerificationDTO
        modelMapper.addMappings(new PropertyMap<CompanyVerification, CompanyVerificationDTO>() {
            @Override
            protected void configure() {
                map().setCompanyId(source.getCompany().getCompanyId());
                map().setSubmitById(source.getAccount().getAccountId());
            }
        });

        // Ánh xạ Member -> MemberDTO
        modelMapper.addMappings(new PropertyMap<Member, MemberDTO>() {
            @Override
            protected void configure() {
                map().setAccountId(source.getAccount().getAccountId());
                map().setCompanyId(source.getCompany().getCompanyId());
            }
        });

        // Ánh xạ Contract -> ContractDTO (sửa để tránh xung đột eventId)
        modelMapper.addMappings(new PropertyMap<Contract, ContractDTO>() {
            @Override
            protected void configure() {
                map().setCompanyId(source.getCompany().getCompanyId());
                map().setCompanyName(source.getCompany().getCompanyName());
                map().setAccountId(source.getAccount().getAccountId());
                map().setEventId(source.getEvent().getEventId()); // Thêm ánh xạ rõ ràng cho eventId
            }
        });

        // Ánh xạ MemberActivity -> MemberActivityDTO
        modelMapper.addMappings(new PropertyMap<MemberActivity, MemberActivityDTO>() {
            @Override
            protected void configure() {
                map().setMemberId(source.getMember().getMemberId());
                map().setEventActivityId(source.getEventActivity().getEventActivityId());
            }
        });

        // Ánh xạ ContractDocument -> ContractDocumentDTO
        modelMapper.addMappings(new PropertyMap<ContractDocument, ContractDocumentDTO>() {
            @Override
            protected void configure() {
                map().setContractId(source.getContract().getContractId());
                map().setUploadedBy(source.getUploadedBy().getAccountId());
            }
        });

        // Ánh xạ Notification -> NotificationDTO
        modelMapper.addMappings(new PropertyMap<Notification, NotificationDTO>() {
            @Override
            protected void configure() {
                map().setAccountId(source.getAccount().getAccountId());
            }
        });

        return modelMapper;
    }
}