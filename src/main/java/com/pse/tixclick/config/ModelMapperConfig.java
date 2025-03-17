package com.pse.tixclick.config;

import com.pse.tixclick.payload.dto.*;
import com.pse.tixclick.payload.entity.Notification;
import com.pse.tixclick.payload.entity.company.*;
import com.pse.tixclick.payload.entity.event.Event;
import com.pse.tixclick.payload.entity.event.EventActivity;
import com.pse.tixclick.payload.entity.ticket.Ticket;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.modelmapper.ModelMapper;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.addMappings(new PropertyMap<Event, EventDTO>() {
            @Override
            protected void configure() {
                map().setCompanyId(source.getCompany().getCompanyId());
                map().setOrganizerId(source.getOrganizer().getAccountId());
                map().setCategoryId(source.getCategory().getEventCategoryId());
            }
        });
        modelMapper.addMappings(new PropertyMap<EventActivity, EventActivityDTO>() {
            @Override
            protected void configure() {
                map().setEventId(source.getEvent().getEventId());
                map().setCreatedBy(source.getCreatedBy().getAccountId());
            }
        });

        modelMapper.addMappings(new PropertyMap<Ticket, TicketDTO>() {
            @Override
            protected void configure() {
                map().setAccountId(source.getAccount().getAccountId());
            }
        });
        modelMapper.addMappings(new PropertyMap<CompanyDocuments, CompanyDocumentDTO>() {
            @Override
            protected void configure() {
                map().setCompanyId(source.getCompany().getCompanyId());
            }
        });
        modelMapper.addMappings(new PropertyMap<CompanyVerification, CompanyVerificationDTO>() {
            @Override
            protected void configure() {
                map().setCompanyId(source.getCompany().getCompanyId());
                map().setSubmitById(source.getAccount().getAccountId());
            }
        });
        modelMapper.addMappings(new PropertyMap<Member, MemberDTO>() {
            @Override
            protected void configure() {
                map().setAccountId(source.getAccount().getAccountId());
                map().setCompanyId(source.getCompany().getCompanyId());
            }


        });
        modelMapper.addMappings(new PropertyMap<Contract, ContractDTO>() {
            @Override
            protected void configure() {
                map().setCompanyId(source.getCompany().getCompanyId());
                map().setAccountId(source.getAccount().getAccountId());
            }
        });
        modelMapper.addMappings(new PropertyMap<MemberActivity, MemberActivityDTO>() {
            @Override
            protected void configure() {
                map().setMemberId(source.getMember().getMemberId());
                map().setEventActivityId(source.getEventActivity().getEventActivityId());
            }
        });
        modelMapper.addMappings(new PropertyMap<ContractDocument, ContractDocumentDTO>() {
            @Override
            protected void configure() {
                map().setContractId(source.getContract().getContractId());
                map().setUploadedBy(source.getUploadedBy().getAccountId());
            }
        });

        modelMapper.addMappings(new PropertyMap<Notification, NotificationDTO>() {
            @Override
            protected void configure() {
                map().setAccountId(source.getAccount().getAccountId());
            }
        });
        return modelMapper;
    }
}