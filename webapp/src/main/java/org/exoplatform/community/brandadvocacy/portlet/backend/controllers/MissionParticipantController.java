package org.exoplatform.community.brandadvocacy.portlet.backend.controllers;

import juzu.*;
import juzu.impl.request.Request;
import juzu.plugin.ajax.Ajax;
import juzu.request.RequestContext;
import juzu.request.RequestParameter;
import juzu.request.SecurityContext;
import org.exoplatform.brandadvocacy.model.*;
import org.exoplatform.brandadvocacy.service.IService;
import org.exoplatform.brandadvocacy.service.Utils;
import org.exoplatform.community.brandadvocacy.portlet.backend.JuZBackEndApplication_;
import org.exoplatform.community.brandadvocacy.portlet.backend.models.MissionDTO;
import org.exoplatform.community.brandadvocacy.portlet.backend.models.MissionParticipantDTO;
import org.exoplatform.community.brandadvocacy.portlet.backend.models.Pagination;
import org.exoplatform.community.brandadvocacy.portlet.backend.models.ParticipantDTO;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by exoplatform on 10/12/14.
 */
@SessionScoped
public class MissionParticipantController {

  final static int NUMBER_RECORDS = 10;
  OrganizationService organizationService;
  IdentityManager identityManager;
  IService missionParticipantService;

  @Inject
  LoginController loginController;
  @Inject
  @Path("mission_participant/error.gtmpl")
  org.exoplatform.community.brandadvocacy.portlet.backend.templates.mission_participant.error ErrorTpl;

  @Inject
  @Path("mission_participant/list.gtmpl")
  org.exoplatform.community.brandadvocacy.portlet.backend.templates.mission_participant.list listTpl;

  @Inject
  @Path("mission_participant/view.gtmpl")
  org.exoplatform.community.brandadvocacy.portlet.backend.templates.mission_participant.view viewTpl;

  @Inject
  @Path("mission_participant/previous.gtmpl")
  org.exoplatform.community.brandadvocacy.portlet.backend.templates.mission_participant.previous previousTPL;


  @Inject
  public MissionParticipantController(OrganizationService organizationService,IdentityManager identityManager ,IService iService){
    this.organizationService = organizationService;
    this.identityManager = identityManager;
    this.missionParticipantService = iService;

  }


  public Response index(){
    String action = WebuiRequestContext.getCurrentInstance().getRequestParameter("action");
    String keyword = WebuiRequestContext.getCurrentInstance().getRequestParameter("keyword");
    String status = WebuiRequestContext.getCurrentInstance().getRequestParameter("stt");
    String page = WebuiRequestContext.getCurrentInstance().getRequestParameter("page");
    String missionParticipantId = WebuiRequestContext.getCurrentInstance().getRequestParameter("id");
    if (null != loginController.getCurrentProgramId()){
      if (null == action || action.equals("mp_search")){
        return this.search(keyword, status,page);
      }else if (action.equals("mp_view") && null != missionParticipantId && !"".equals(missionParticipantId) ){
        return this.view(missionParticipantId);
      }
    }

    return ErrorTpl.with().set("msg","Cannot find mission participant").ok();

  }
  public Response list(){
    String programId = loginController.getCurrentProgramId();
    List<MissionParticipant>  missionParticipants = this.missionParticipantService.getAllMissionParticipantsInProgram(programId);
    List<MissionParticipantDTO> missionParticipantDTOs = this.transfers2DTOs(missionParticipants);
    return listTpl.with().set("missionParticipantDTOs",missionParticipantDTOs).set("states", Status.values()).ok();
  }

  @View
  public Response.Content view(String missionParticipantId){
    MissionParticipant missionParticipant = this.missionParticipantService.getMissionParticipantById(missionParticipantId);
    if(null != missionParticipant){
      try {
        Mission mission = this.missionParticipantService.getMissionById(missionParticipant.getMission_id());
        if(null != mission){
          Identity identity = this.identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,missionParticipant.getParticipant_username(),true);
          if (null != identity){
            MissionParticipantDTO missionParticipantDTO = new MissionParticipantDTO();
            missionParticipantDTO.setId(missionParticipantId);
            missionParticipantDTO.setMission_title(mission.getTitle()+" on "+mission.getThird_part_link());
            missionParticipantDTO.setSize(missionParticipant.getSize().getLabel());
            missionParticipantDTO.setDate_submitted(Utils.convertDateFromLong(missionParticipant.getModifiedDate()));
            missionParticipantDTO.setStatus(missionParticipant.getStatus());
            missionParticipantDTO.setUrl_submitted(missionParticipant.getUrl_submitted());

            ParticipantDTO participantDTO = new ParticipantDTO();
            participantDTO.setFullName(identity.getProfile().getFullName());
            participantDTO.setUrlAvatar(identity.getProfile().getAvatarUrl());
            participantDTO.setUrlProfile(identity.getProfile().getUrl());
            participantDTO.setEmail(identity.getProfile().getEmail());
            Address address = this.missionParticipantService.getAddressById(missionParticipant.getAddress_id());
            if (null == address){
              address = new Address("","","","","","");
            }
            return viewTpl.with().set("missionParticipantDTO",missionParticipantDTO).set("address",address).set("participantDTO",participantDTO).set("states",Status.values()).ok();

          }
        }

      } catch (Exception e) {
        return ErrorTpl.with().set("msg","Cannot find mission participant").ok();
      }
    }
    return ErrorTpl.with().set("msg","Cannot find mission participant").ok();
  }

  @View
  public Response.Content search(String keyword,String status,String page){
    String programId = loginController.getCurrentProgramId();
    Query query = new Query(programId);
    query.setKeyword(keyword);
    query.setStatus(status);
    query.setOffset(page);
    query.setLimit(NUMBER_RECORDS);
    List<MissionParticipant>  missionParticipants = this.missionParticipantService.searchMissionParticipants(query);
    List<MissionParticipantDTO> missionParticipantDTOs = this.transfers2DTOs(missionParticipants);
    Pagination pagination = new Pagination(this.missionParticipantService.getTotalMissionParticipants(query),NUMBER_RECORDS,page);
    return listTpl.with().set("missionParticipantDTOs",missionParticipantDTOs).set("states", Status.values()).set("keyword",query.getKeyword()).set("statusFilter",query.getStatus()).set("pagination",pagination).ok();

  }

  @Ajax
  @Resource
  public Response ajaxUpdateMPInline(String missionParticipantId,String action,String val){
    Boolean hasError = false;
    String msg = "";
    if (loginController.isShippingManager()){

      if (Status.SHIPPED.getValue() != Integer.parseInt(val)) {
        hasError = true;
        msg = "you have no rights for this change";
      }
    }else if (loginController.isValidator()){
      if (Status.SHIPPED.getValue() == Integer.parseInt(val)){
        hasError = true;
        msg = "you have no rights for this change";
      }
    }
    if (hasError){
      return Response.ok(msg);
    }
    MissionParticipant missionParticipant = this.missionParticipantService.getMissionParticipantById(missionParticipantId);
    if (null != missionParticipant){
      if (action.equals("status"))
        missionParticipant.setStatus(Status.getStatus(Integer.parseInt(val)));

      missionParticipant = this.missionParticipantService.updateMissionParticipantInProgram(loginController.getCurrentProgramId(),missionParticipant);
      if (null != missionParticipant)
        return Response.ok("ok");
    }
    return Response.ok("something went wrong");
  }

  private List<MissionParticipantDTO> transfers2DTOs(List<MissionParticipant> missionParticipants){
    List<MissionParticipantDTO> missionParticipantDTOs = new ArrayList<MissionParticipantDTO>();
    MissionParticipantDTO missionParticipantDTO;
    Mission mission;
    User exoUser;
    for (MissionParticipant missionParticipant : missionParticipants){
      try {
        exoUser = this.organizationService.getUserHandler().findUserByName(missionParticipant.getParticipant_username());
        if(null != exoUser){
          mission = this.missionParticipantService.getMissionById(missionParticipant.getMission_id());
          if (null != mission){
            missionParticipantDTO = new MissionParticipantDTO();
            missionParticipantDTO.setId(missionParticipant.getId());
            missionParticipantDTO.setMission_title(mission.getTitle());
            missionParticipantDTO.setParticipant_fullName(exoUser.getFirstName()+ " "+exoUser.getLastName());
            missionParticipantDTO.setStatus(missionParticipant.getStatus());
            missionParticipantDTO.setUrl_submitted(missionParticipant.getUrl_submitted());
            missionParticipantDTO.setDate_submitted(Utils.convertDateFromLong(missionParticipant.getModifiedDate()));
            missionParticipantDTOs.add(missionParticipantDTO);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return missionParticipantDTOs;
  }

  @Ajax
  @Resource
  public Response getPreviousMissionParticipant(String username){
    String programId = loginController.getCurrentProgramId();
    List<MissionParticipant> missionParticipants = this.missionParticipantService.getAllMissionParticipantsInProgramByParticipant(programId,username);
    List<MissionParticipantDTO> missionParticipantDTOs = new ArrayList<MissionParticipantDTO>();
    MissionParticipantDTO missionParticipantDTO;
    Mission mission;
    for (MissionParticipant missionParticipant : missionParticipants){
      mission = this.missionParticipantService.getMissionById(missionParticipant.getMission_id());
      if (null != mission){
        missionParticipantDTO = new MissionParticipantDTO();
        missionParticipantDTO.setId(missionParticipant.getId());
        missionParticipantDTO.setMission_title(mission.getTitle());
        missionParticipantDTO.setDate_submitted(Utils.convertDateFromLong(missionParticipant.getModifiedDate()));
        missionParticipantDTOs.add(missionParticipantDTO);
      }
    }
    return previousTPL.with().set("missionParticipantDTOs",missionParticipantDTOs).ok();
  }
}
