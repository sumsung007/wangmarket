package com.xnx3.wangmarket.admin.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xnx3.StringUtil;
import com.xnx3.j2ee.service.SqlService;
import com.xnx3.j2ee.util.Page;
import com.xnx3.j2ee.util.Sql;
import com.xnx3.j2ee.vo.BaseVO;
import com.xnx3.j2ee.vo.UploadFileVO;
import com.xnx3.wangmarket.admin.Func;
import com.xnx3.wangmarket.admin.G;
import com.xnx3.wangmarket.admin.cache.GenerateHTML;
import com.xnx3.wangmarket.admin.cache.Template;
import com.xnx3.wangmarket.admin.cache.pc.IndexAboutUs;
import com.xnx3.wangmarket.admin.cache.pc.IndexNews;
import com.xnx3.wangmarket.admin.entity.InputModel;
import com.xnx3.wangmarket.admin.entity.News;
import com.xnx3.wangmarket.admin.entity.NewsData;
import com.xnx3.wangmarket.admin.entity.Site;
import com.xnx3.wangmarket.admin.entity.SiteColumn;
import com.xnx3.wangmarket.admin.entity.TemplatePage;
import com.xnx3.wangmarket.admin.service.InputModelService;
import com.xnx3.wangmarket.admin.service.NewsService;
import com.xnx3.wangmarket.admin.service.SiteColumnService;
import com.xnx3.wangmarket.admin.service.SiteService;
import com.xnx3.wangmarket.admin.service.TemplateService;
import com.xnx3.wangmarket.admin.util.AliyunLog;
import com.xnx3.j2ee.func.AttachmentFile;
import com.xnx3.wangmarket.admin.vo.SiteColumnTreeVO;
import com.xnx3.wangmarket.admin.vo.TemplatePageListVO;

/**
 * 公共的
 * @author 管雷鸣
 */
@Controller
@RequestMapping("/column")
public class ColumnController extends BaseController {
	@Resource
	private SqlService sqlService;
	@Resource
	private SiteService siteService;
	@Resource
	private NewsService newsService;
	@Resource
	private SiteColumnService siteColumnService;
	@Resource
	private TemplateService templateService;
	@Resource
	private InputModelService inputModelService;
	
	/**
	 * 栏目列表
	 */
	@RequestMapping("/list${url.suffix}")
	public String list(HttpServletRequest request,Model model){
	    Sql sql = new Sql(request);
	    sql.setSearchTable("site_column");
	    //增加添加搜索字段。这里的搜索字段跟log表的字段名对应
	    sql.setSearchColumn(new String[]{});
	    sql.appendWhere("userid = "+getUserId());
	    //查询log数据表的记录总条数
	    int count = sqlService.count("site_column", sql.getWhere());
	    //每页显示300条
	    Page page = new Page(count, 300, request);
	    //创建查询语句，只有SELECT、FROM，原生sql查询。其他的where、limit等会自动拼接
	    sql.setSelectFromAndPage("SELECT * FROM site_column", page);
	    
	    //当用户没有选择排序方式时，系统默认排序。
	    sql.setDefaultOrderBy("site_column.rank ASC");
	    //因联合查询，结果集是没有实体类与其对应，故而用List<Map>接收
	    List<SiteColumn> list = sqlService.findBySql(sql, SiteColumn.class);
	    
	    AliyunLog.addActionLog(getSiteId(), "查看栏目列表");
	    
	    //将数据记录传到页面以供显示
	    model.addAttribute("list", list);
	    //将分页信息传到页面以供显示
	    model.addAttribute("page", page);
	    model.addAttribute("siteid", getSiteId());
	    model.addAttribute("AttachmentFileUrl", AttachmentFile.netUrl());
	    return "column/list";
	}
	
	/**
	 * 获取当前操作的网站下的栏目，无需传入siteid
	 * 针对PC端通用模版模式下
	 */
	@RequestMapping("/popupList${url.suffix}")
	public String popupList(HttpServletRequest request,Model model){
	    Sql sql = new Sql(request);
	    sql.setSearchTable("site_column");
	    //增加添加搜索字段。这里的搜索字段跟log表的字段名对应
	    sql.setSearchColumn(new String[]{});
	    sql.appendWhere("siteid = "+getSiteId()+" AND userid = "+getUserId());
	    //查询log数据表的记录总条数
	    int count = sqlService.count("site_column", sql.getWhere());
	    //每页显示300条
	    Page page = new Page(count, 300, request);
	    //创建查询语句，只有SELECT、FROM，原生sql查询。其他的where、limit等会自动拼接
	    sql.setSelectFromAndPage("SELECT * FROM site_column", page);
	    
	    //当用户没有选择排序方式时，系统默认排序。
	    sql.setDefaultOrderBy("site_column.rank ASC");
	    //因联合查询，结果集是没有实体类与其对应，故而用List<Map>接收
	    List<SiteColumn> list = sqlService.findBySql(sql, SiteColumn.class);
	    
	    AliyunLog.addActionLog(getSiteId(), "针对PC端通用模版模式下，查看栏目列表");
	    
	    //将数据记录传到页面以供显示
	    model.addAttribute("list", list);
	    //将分页信息传到页面以供显示
	    model.addAttribute("page", page);
	    model.addAttribute("siteid", getSiteId());
	    model.addAttribute("site", getSite());
	    model.addAttribute("cursorStyle", (getSite().getTemplateName()!=null && getSite().getTemplateName().length()>0) ? "":"cursor: all-scroll;");
	    model.addAttribute("AttachmentFileUrl", AttachmentFile.netUrl());
	    return "column/popup_list";
	}
	
	/**
	 * CMS模式下，获取当前操作的网站下的栏目，无需传入siteid
	 */
	@RequestMapping("/popupListForTemplate${url.suffix}")
	public String popupListForTemplate(HttpServletRequest request,Model model){
		List<SiteColumnTreeVO> list = siteColumnService.getSiteColumnTreeVOByCache();
	    
		AliyunLog.addActionLog(getSiteId(), "CMS模式下，获取网站下的栏目列表");
		
	    //将数据记录传到页面以供显示
	    model.addAttribute("list", list);
	    //将分页信息传到页面以供显示
	    model.addAttribute("site", getSite());
	    return "column/popupListForTemplate";
	}
	
	/**
	 * 添加/编辑栏目导航
	 * @param id 要修改的栏目id，若为0或空，则是添加栏目
	 */
	@RequestMapping("/column${url.suffix}")
	public String column(HttpServletRequest request,
			@RequestParam(value = "id", required = false , defaultValue="0") int id,
			Model model){
		Site site = getSite();
		String title = "";
		SiteColumn siteColumn = null;
		String iconImage = "";	//当前使用的图标
		if(id > 0){
			//修改
			siteColumn = sqlService.findById(SiteColumn.class, id);
			if(siteColumn == null){
				return error(model, "要修改的栏目导航不存在！");
			}
			if(siteColumn.getSiteid() - site.getId() != 0){
				return error(model, "栏目不属于您，无法修改！");
			}
		}else{
			siteColumn = new SiteColumn();
			siteColumn.setType(SiteColumn.TYPE_PAGE);
			siteColumn.setUsed(SiteColumn.USED_ENABLE);
		}
		model.addAttribute("siteColumn", siteColumn);
		model.addAttribute("site", site);
		
		if(id > 0){
			String icon = siteColumn.getIcon().indexOf("://")==-1? AttachmentFile.netUrl()+"site/"+site.getId()+"/column_icon/"+siteColumn.getIcon():siteColumn.getIcon();
			iconImage = "<img src=\""+icon+"\" height=\"30\" onclick=\"window.open('"+icon+"');\" alt=\"当前的图标\" style=\"cursor:pointer;\">";
		}
		model.addAttribute("iconImage", iconImage);
		
		AliyunLog.addActionLog(getSiteId(), "进入添加、编辑导航栏目页面");
		
		siteService.getTemplateCommonHtml(site, title, model);
		return "column/column";
	}
	
	/**
	 * 弹出框更该栏目，简单，只是更改栏目名字
	 * 适用于通用电脑网站模式下
	 * @param id 栏目id
	 */
	@RequestMapping("/popupColumnUpdate${url.suffix}")
	public String popupColumnUpdate(HttpServletRequest request,
			@RequestParam(value = "id", required = false , defaultValue="0") int id,
			Model model){
		Site site = getSite();
		SiteColumn siteColumn = sqlService.findById(SiteColumn.class , id);
		if(siteColumn == null){
			return error(model, "要修改的栏目导航不存在！");
		}
		if(siteColumn.getUserid() - getUserId() != 0){
			return error(model, "栏目不属于你，无法修改");
		}
		
		String icon = siteColumn.getIcon().indexOf("://")==-1? AttachmentFile.netUrl()+"site/"+site.getId()+"/column_icon/"+siteColumn.getIcon():siteColumn.getIcon();
		
		AliyunLog.addActionLog(getSiteId(), "通用电脑网站模式下，弹出更该栏目名字的弹出框");
		
		model.addAttribute("icon", icon);
		model.addAttribute("siteColumn", siteColumn);
		return "column/popup_column";
	}
	
	/**
	 * 弹出框更该栏目，高级，栏目名字、图标、类型、是否显示等
	 * 通用电脑模式下，高级更改栏目
	 * @param id 栏目id，若是添加，忽略此处
	 */
	@RequestMapping("/popupColumnGaoJiUpdate${url.suffix}")
	public String popupColumnGaoJiUpdate(HttpServletRequest request,
			@RequestParam(value = "id", required = false , defaultValue="0") int id,
			Model model){
		Site site = getSite();
		
		SiteColumn siteColumn;
		if(id > 0){
			//修改
			siteColumn = sqlService.findById(SiteColumn.class , id);
			if(siteColumn == null){
				return error(model, "要修改的栏目导航不存在！");
			}
			if(siteColumn.getUserid() - getUserId() != 0){
				return error(model, "栏目不属于你，无法修改");
			}
		}else{
			//创建
			siteColumn = new SiteColumn();
		}
		
		
		String icon = siteColumn.getIcon().indexOf("://")==-1? AttachmentFile.netUrl()+"site/"+site.getId()+"/column_icon/"+siteColumn.getIcon():siteColumn.getIcon();
		
		AliyunLog.addActionLog(getSiteId(), "通用电脑网站模式下，打开更该栏目属性的页面");
		
		model.addAttribute("icon", icon);
		model.addAttribute("siteColumn", siteColumn);
		model.addAttribute("site", getSite());
		return "column/popup_columnGaoJi";
	}
	
	/**
	 * CMS模式建站，添加／修改栏目
	 * @param id 栏目id，若是修改栏目，需要传入其id，若是添加，不用理会
	 * @param isCopy 是否是要复制一个栏目来创建  0:不是复制     1:复制一个目标栏目来快速创建栏目，此时要清空siteColumn.id为null
	 */
	@RequestMapping("/popupColumnForTemplate${url.suffix}")
	public String popupColumnForTemplate(HttpServletRequest request,Model model,
			@RequestParam(value = "id", required = false , defaultValue="0") int id,
			@RequestParam(value = "isCopy", required = false , defaultValue="0") int isCopy){
		Site site = getSite();
		
		SiteColumn siteColumn;
		if(id > 0){
			//修改操作
			siteColumn = sqlService.findById(SiteColumn.class , id);
			if(siteColumn == null){
				return error(model, "要修改的栏目导航不存在！");
			}
			if(siteColumn.getUserid() - getUserId() != 0){
				return error(model, "栏目不属于你，无法修改");
			}
			
			if(isCopy == 1){
				/**
				 * 复制栏目
				 * 		1.要将id清空，才能创建新栏目
				 * 		2.将栏目代码末尾自动加上个1，以区分
				 * 		3.将栏目名改为“复制＋原栏目名字”
				 */
				try {
					SiteColumn sc = siteColumn.clone();
					siteColumn = null;
					siteColumn = sc;
					siteColumn.setId(null);
					if(siteColumn.getCodeName() != null){
						siteColumn.setCodeName(siteColumn.getCodeName()+"1");
					}
					siteColumn.setName("复制"+siteColumn.getName());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}else{
			//添加操作
			siteColumn = new SiteColumn();
			siteColumn.setType(SiteColumn.TYPE_NEWS);//默认为新闻类型
			siteColumn.setParentid(0); //默认为顶级栏目
		}

		//获取用户当前的模版页面列表
		TemplatePageListVO templatePageListVO = templateService.getTemplatePageListByCache(request);
		
		//内容模版
		StringBuffer tpl_view_option = new StringBuffer();
		for (int i = 0; i < templatePageListVO.getList().size(); i++) {
			TemplatePage tp = templatePageListVO.getList().get(i).getTemplatePage();
			if(tp.getType() - TemplatePage.TYPE_NEWS_VIEW == 0 || tp.getType() - TemplatePage.TYPE_ALONEPAGE == 0){
				if(siteColumn.getTemplatePageViewName() != null && siteColumn.getTemplatePageViewName().equals(tp.getName())){
					tpl_view_option.append("<option value=\""+tp.getName()+"\" selected=\"selected\">"+tp.getName()+","+tp.getRemark()+"</option>");
				}else{
					tpl_view_option.append("<option value=\""+tp.getName()+"\">"+tp.getName()+","+tp.getRemark()+"</option>");
				}
			}
		}
		if(tpl_view_option.length() == 0){
			//如果没有内容模版，那么会默认选择(显示)一个提示，暂无可选的内容模版
			tpl_view_option.append("<option value=\"\" selected=\"selected\">暂无可选的内容模版</option>");
		}
		model.addAttribute("tpl_view_option", tpl_view_option.toString());
		
		//选择出列表使用的模版。变为<select>中使用的option
		StringBuffer tpl_list_option = new StringBuffer();
		for (int i = 0; i < templatePageListVO.getList().size(); i++) {
			TemplatePage tp = templatePageListVO.getList().get(i).getTemplatePage();
			if(tp.getType() - TemplatePage.TYPE_NEWS_LIST == 0){
				//判断用户当前是否是使用了这个模版
				if(siteColumn.getTemplatePageListName() != null && siteColumn.getTemplatePageListName().equals(tp.getName())){
					tpl_list_option.append("<option value=\""+tp.getName()+"\" selected=\"selected\">"+tp.getName()+","+tp.getRemark()+"</option>");
				}else{
					tpl_list_option.append("<option value=\""+tp.getName()+"\">"+tp.getName()+","+tp.getRemark()+"</option>");
				}
			}
		}
		if(tpl_list_option.length() == 0){
			//如果没有内容模版，那么会默认选择(显示)一个提示，暂无可选的内容模版
			tpl_list_option.append("<option value=\"\" selected=\"selected\">暂无可选的列表模版</option>");
		}
		model.addAttribute("tpl_list_option", tpl_list_option.toString());
		
		
		//获取当前用户的一级栏目，方便选择父栏目。这里直接从缓存中取得栏目树
		List<SiteColumnTreeVO> siteColumnTreeList = siteColumnService.getSiteColumnTreeVOByCache();
		StringBuffer parentColumn = new StringBuffer();
		if((siteColumn.getParentid() != null && siteColumn.getParentid() > 0) || (siteColumn.getParentCodeName() != null && siteColumn.getParentCodeName().length() > 0)){
			//若parentid 或者 parentName 有值，则是二级栏目
			parentColumn.append("<option value=\"\">顶级栏目</option>");
		}else{
			parentColumn.append("<option value=\"\" \" selected=\"selected\">顶级栏目</option>");
		}
		for (int i = 0; i < siteColumnTreeList.size(); i++) {
			SiteColumn sc = siteColumnTreeList.get(i).getSiteColumn();
			
			//判断，若是当前栏目也是顶级栏目，要将当前栏目隐去。上级栏目不会是当前栏目
			if(siteColumn.getId() != null && sc.getId() - siteColumn.getId() == 0){
				//若是，不输出
			}else{
				//判断当前是否是在此顶级栏目下。依据便是根据栏目代码来进行对比
				if(sc.getCodeName() != null && siteColumn.getParentCodeName() != null && siteColumn.getParentCodeName().equals(sc.getCodeName())){
					parentColumn.append("<option value=\""+sc.getCodeName()+"\" selected=\"selected\">"+sc.getName()+"</option>");
				}else{
					parentColumn.append("<option value=\""+sc.getCodeName()+"\">"+sc.getName()+"</option>");
				}
				
			}
		}
		
		//输入模型，转为select标签内的option
		List<InputModel> inputModelList = inputModelService.getInputModelListForSession();
		//输入模型的option标签集合，select中
		String inputModelOptions = "";
		//获取到当前栏目所使用的输入模型
		String imCodeName = null;
		if(siteColumn.getInputModelCodeName() != null && siteColumn.getInputModelCodeName().length() > 0){
			imCodeName = siteColumn.getInputModelCodeName();
		}
		//遍历出option，同时取到默认选中的输入模型
		boolean findDefaultModel = false;	//for中发现了默认的输入模型，默认为false，没有发现。若没有发现，for之后，就要将系统输入模型变为默认的输入模型
		for (int i = 0; i < inputModelList.size(); i++) {
			InputModel inputModel = inputModelList.get(i);
			if(imCodeName != null && inputModel.getCodeName().equals(imCodeName)){
				//默认选中的，当前栏目使用的
				findDefaultModel = true;
				inputModelOptions = inputModelOptions + "<option value=\""+ inputModel.getCodeName() +"\" selected=\"selected\">"+ inputModel.getCodeName()+","+inputModel.getRemark() +"</option>";
			}else{
				//没有使用的其他输入模型
				inputModelOptions = inputModelOptions + "<option value=\""+ inputModel.getCodeName() +"\">"+ inputModel.getCodeName()+","+inputModel.getRemark() +"</option>";
			}
		}
		//判断for中是否找到了默认选中的输入模型，若没找到，则需要将系统输入模型作为默认的输入模型
		if(!findDefaultModel){
			inputModelOptions = "<option value=\"0\" selected=\"selected\">系统内置模型</option>" + inputModelOptions;
		}else{
			inputModelOptions = "<option value=\"0\">系统内置模型</option>" + inputModelOptions;
		}
		
		AliyunLog.addActionLog(getSiteId(), "CMS模式下，添加、修改栏目");
		
		model.addAttribute("parentColumnOption", parentColumn.toString());
		model.addAttribute("site", site);
		model.addAttribute("siteColumn", siteColumn);
		model.addAttribute("inputModelOptions", inputModelOptions);
		return "column/popupColumnForTemplate";
	}
	
	/**
	 * 更改栏目，提交
	 * 
	 */
	@RequestMapping(value="/popupColumnUpdateSubmit${url.suffix}", method = RequestMethod.POST)
	@ResponseBody
	public BaseVO popupColumnUpdateSubmit(HttpServletRequest request, SiteColumn sc){
		SiteColumn siteColumn = sqlService.findById(SiteColumn.class , sc.getId());
		if(siteColumn == null){
			return error("栏目不存在");
		}
		if(siteColumn.getUserid() - getUserId() != 0){
			return error("栏目不属于你，无法修改");
		}
		Site site = getSite();
		boolean updateName = !sc.getName().equals(siteColumn.getName());	//是否有过修改名字，若有过修改，则为true
		
		siteColumn.setName(filter(sc.getName()));
		sqlService.save(siteColumn);
		
		AliyunLog.addActionLog(siteColumn.getId(), "保存栏目："+siteColumn.getName());
		
		//如果这个栏目是独立页面，那么判断是否有了这个独立页面，若没有，自动建立一个
		if(siteColumn.getType() == SiteColumn.TYPE_PAGE){
			siteColumnService.createNonePage(siteColumn,site,updateName);
		}
		//生成栏目页面
		if(siteColumn.getType() == SiteColumn.TYPE_NEWS || siteColumn.getType() == SiteColumn.TYPE_IMAGENEWS){
			newsService.generateListHtml(site, siteColumn);
		}
		
		//更新栏目导航的js缓存数据
		List<SiteColumn> list = sqlService.findBySqlQuery("SELECT * FROM site_column WHERE siteid = "+site.getId()+" AND used = "+SiteColumn.USED_ENABLE+" ORDER BY rank ASC", SiteColumn.class);
		new com.xnx3.wangmarket.admin.cache.Site().siteColumn(list,site);
		
		//如果名字更改了，那么，更改首页相应得名字
		if(updateName){
			//看看首页是否调用了该栏目，还要修改首页的数据
			//判断该栏目是否在首页出现，若出现过了，还要相应的修改首页的，该栏目的信息
			GenerateHTML gh = new GenerateHTML(site);
			String indexHtml = gh.getGeneratePcIndexHtml();
			if(Template.isAnnoConfigById_Have(indexHtml, siteColumn.getId())){
				//判断是独立页面，还是列表页面
				if(siteColumn.getType() - SiteColumn.TYPE_PAGE == 0){
					//独立页面
					//判断，只有当是关于我们时，才会刷新首页
					if(site.getAboutUsCid() - siteColumn.getId() == 0){
						News news = (News) sqlService.findAloneBySqlQuery("SELECT * FROM news WHERE cid = "+siteColumn.getId(), News.class);
						NewsData newsData = (NewsData) sqlService.findById(NewsData.class, news.getId());
						IndexAboutUs.refreshIndexData(site, siteColumn, news, newsData.getText());
					}
				}else if (siteColumn.getType() - SiteColumn.TYPE_IMAGENEWS == 0 || siteColumn.getType() - SiteColumn.TYPE_NEWS == 0) {
					//若存在了，需要修改，重新刷新此栏目再首页的列表模块信息
					List<News> newsList = sqlService.findBySqlQuery("SELECT * FROM news WHERE cid = "+siteColumn.getId() + " AND status = "+News.STATUS_NORMAL+" ORDER BY id DESC LIMIT 0,10", News.class);
					IndexNews.refreshIndexData(site, siteColumn, newsList);
				}
			}
		}
		
		return success();
	}
	

	/**
	 * 创建/修改栏目导航保存，用于 popupColumnGaoJiUpdate、CMS模式编辑栏目的保存
	 * @throws CloneNotSupportedException 
	 */
	@RequestMapping(value="savePopupColumnGaoJiUpdate${url.suffix}", method = RequestMethod.POST)
	@ResponseBody
	public BaseVO savePopupColumnGaoJiUpdate(SiteColumn siteColumn,HttpServletRequest request,Model model){
		BaseVO vo = new BaseVO();
		Site site = getSite();
		if(site == null){
			vo.setBaseVO(BaseVO.FAILURE, "要修改的导航栏目所属的站点不存在！");
			return vo;
		}
		if(site.getUserid() != getUserId()){
			vo.setBaseVO(BaseVO.FAILURE, "站点不属于您，无法修改！");
			return vo;
		}
		
		//标题，名字
		String name = filter(siteColumn.getName());
		if(name == null || name.length()<1){
			vo.setBaseVO(BaseVO.FAILURE, "您要创建的导航栏目叫什么名字呢");
			return vo;
		}
		
		//记录是否是新增导航栏目
		boolean addColumn = true;
		boolean updateName = false;		//是否有过修改名字，若修改了名字了，还要吧内容页面的也一块修改
		SiteColumn sc = new SiteColumn();
		String oldCodeName = null;	//旧的栏目代码内容，数据库中原本存储的栏目代码的内容。若为null，则是新增的
		if(siteColumn.getId() != null && siteColumn.getId() > 0){
			addColumn = false;
			sc = sqlService.findById(SiteColumn.class, siteColumn.getId());
			if(sc.getSiteid() - site.getId() != 0){
				vo.setBaseVO(BaseVO.FAILURE, "要修改的栏目不属于您，无权修改");
				return vo;
			}
			if(!sc.getName().equals(name)){
				updateName = true;
			}
			oldCodeName = sc.getCodeName();
		}else{
			sc.setUserid(getUserId());
			sc.setSiteid(site.getId());
			sc.setRank(0);
		}
		
		sc.setName(name);
//		sc.setRank(siteColumn.getRank());
		sc.setUrl(filter(siteColumn.getUrl()));
		sc.setUsed(siteColumn.getUsed() == null? 1:siteColumn.getUsed());
		sc.setType(siteColumn.getType());
		sc.setTemplatePageListName(filter(siteColumn.getTemplatePageListName()));
		sc.setTemplatePageViewName(filter(siteColumn.getTemplatePageViewName()));
		sc.setListNum(siteColumn.getListNum() == null ? 10:siteColumn.getListNum());
		sc.setEditMode(siteColumn.getEditMode() == null ? SiteColumn.EDIT_MODE_INPUT_MODEL : siteColumn.getEditMode());
		sc.setListRank(siteColumn.getListRank() == null? SiteColumn.LIST_RANK_ADDTIME_DESC:siteColumn.getListRank());
		
		//判断一下选择的输入模型是否符合
		String inputModelCodeName = filter(siteColumn.getInputModelCodeName());
		if(inputModelCodeName == null || inputModelCodeName.length() == 0 || inputModelCodeName.equals("0")){
			//使用系统默认输入模型(为0代表是系统模型，因为layui中，如果value没有值的话，系统模型是无法出现选择的)
			sc.setInputModelCodeName(null);
		}else{
			//使用自定义输入模型，那么先判断这个输入模型的模型代码是否存在，存在才能保存
			boolean isExist = false;	//这个输入模型代码是否存在。默认不存在
			//从缓存中取输入模型列表
			List<InputModel> inputModelList = inputModelService.getInputModelListForSession();
			for (int i = 0; i < inputModelList.size(); i++) {
				InputModel im = inputModelList.get(i);
				if(im.getCodeName().equals(inputModelCodeName)){
					isExist = true;
					break;
				}
			}
			
			if(!isExist){
				return error("您选择的该栏目的输入模型不存在！请重试。");
			}else{
				sc.setInputModelCodeName(inputModelCodeName);
			}
		}
		
		
		if(Func.isCMS(site)){
			//只有当使用高级自定义模版时，才会有多极栏目
			//判断是否更改了父栏目,
			//如果原本的父栏目代码是为空的，而现在的不为空，又或者他俩不相等，那就是有更改了
			if(sc.getParentCodeName() == null && siteColumn.getParentCodeName() != null || (!sc.getParentCodeName().equals(siteColumn.getParentCodeName()))){
				if(siteColumn.getParentCodeName().length() == 0){
					//清空，也就是将其设置为一级栏目
					sc.setParentCodeName("");
					sc.setParentid(0);
				}else{
					//查询parentCodeName是否存在
					SiteColumn s = (SiteColumn) sqlService.findAloneBySqlQuery("SELECT * FROM site_column WHERE siteid="+site.getId()+" AND code_name ='"+Sql.filter(siteColumn.getParentCodeName())+"'", SiteColumn.class);
					if(s == null){
						vo.setBaseVO(BaseVO.FAILURE, "您填写的父栏目代码没有找到指定的栏目，请检查您是否有栏目代码为"+siteColumn.getParentCodeName()+"的栏目");
						return vo;
					}else{
						/*
							要给当前栏目设定父栏目，首先，现在的系统只支持二级栏目，
							若是新增，那没事。若是修改，得判断当前修改的栏目是否有下级栏目了。
							若是当前修改的栏目还有下级栏目，此栏目是没法再设定上级栏目的
						*/
						if(addColumn == false && sqlService.count("site_column", "WHERE parentid = "+siteColumn.getId()) > 0){
							return error("目前系统暂时只支持一级子栏目，当前此栏目已经有下级栏目了，无法再作为子栏目");
						}
						
						sc.setParentCodeName(filter(siteColumn.getParentCodeName()));
						sc.setParentid(s.getId());
					}
				}
			}
			
			//判断是否更改了当前栏目代码，同父栏目编码的判断。当此栏目的栏目代码有修改时，才会进入以下判断
			if( (sc.getCodeName() == null && siteColumn.getCodeName() != null) || (sc.getCodeName() != null && siteColumn.getCodeName() == null) || (!sc.getCodeName().equals(siteColumn.getCodeName())) ){
				if(siteColumn.getCodeName().length() == 0){
					sc.setCodeName("");
				}else{
					sc.setCodeName(filter(siteColumn.getCodeName()));
					
					//查询当前网站codeName是否被占用了，v2.26更改，有数据库查询改为从内存中判断
					List<SiteColumn> sclistCache = siteColumnService.getSiteColumnListByCache();
					for (int i = 0; i < sclistCache.size(); i++) {
						SiteColumn s = sclistCache.get(i);
						if(s.getCodeName().equals(sc.getCodeName())){
							if(sc.getId() != null && sc.getId() - s.getId() == 0){
								//是当前栏目，略过
							}else{
								//不是当前栏目，那么就是有重复了
								vo.setBaseVO(BaseVO.FAILURE, "您当前的栏目代码已经有栏目使用了，请换一个吧，网站内栏目代码是唯一的");
								return vo;
							}
						}
					}
					
				}
			}
		}else{
			sc.setParentid(0);		//父栏目id，通用模版只有一级栏目
		}
		
		String oldIconName = sc.getIcon();	//旧的栏目导航图名字
		
		//上传图标，并进行压缩处理
		UploadFileVO upload= AttachmentFile.uploadImage("site/"+site.getId()+"/column_icon/", request, "iconFile", G.SITECOLUMN_ICON_MAXWIDTH);
		if(upload.getResult() == BaseVO.SUCCESS){
			sc.setIcon(upload.getFileName());
		}

		sqlService.save(sc);
		if(sc.getId() > 0){
			//如果这个栏目是独立页面，那么判断是否有了这个独立页面，若没有，自动建立一个
			if(sc.getType() == SiteColumn.TYPE_PAGE){
				//判断一下，这个独立页面的内容编辑方式，如果是模版编辑方式，那么是不用创建news的
				if(sc.getEditMode() - SiteColumn.EDIT_MODE_TEMPLATE == 0){
					//模版编辑方式，忽略
				}else{
					//富文本编辑框或者输入模型编辑方式，则要创建独立页面
					siteColumnService.createNonePage(sc,site,updateName);
				}
			}
			
			if(Func.isCMS(site)){
				//高级自定义模版
				//如果栏目代码有改动了，那么此栏目下的子栏目的parentCodeName也要改动
				if(oldCodeName != null && !sc.getCodeName().equals(oldCodeName)){
					sqlService.executeSql("UPDATE site_column SET parent_code_name = '"+Sql.filter(sc.getCodeName())+"' WHERE parentid = "+sc.getId());
				}
				
				// //这个栏目改动完毕后，要重新将此栏目加入Session缓存中去
				siteColumnService.updateSiteColumnByCache(sc);
			}else{
				//全自动生成模式的系统模版
				
				//生成栏目页面
				if(sc.getType() == SiteColumn.TYPE_NEWS || sc.getType() == SiteColumn.TYPE_IMAGENEWS){
					newsService.generateListHtml(site, sc);
				}
				
				//栏目排序，新增加的放到最后一个
				if(addColumn){
					new com.xnx3.wangmarket.admin.cache.Site().siteColumnRankAppend(site, sc.getId());
				}
				
				//更新site中存储的栏目id
				String siteColumnId = siteService.getSiteColumnId(sc, site);
				if(siteColumnId != null){
					site.setColumnId(siteColumnId);
					sqlService.save(site);
				}
				
				//更新栏目导航的js缓存数据
				List<SiteColumn> list = sqlService.findBySqlQuery("SELECT * FROM site_column WHERE siteid = "+site.getId()+" AND used = "+SiteColumn.USED_ENABLE+" ORDER BY rank ASC", SiteColumn.class);
				new com.xnx3.wangmarket.admin.cache.Site().siteColumn(list,site);
				
				//判断是否更改了栏目名字，若是更改了，还要看看首页是否调用了该栏目，还要修改首页的数据
				if(sc.getName().equals(siteColumn.getName())){
					//判断该栏目是否在首页出现，若出现过了，还要相应的修改首页的，该栏目的信息
					Template template = new Template(site);
					GenerateHTML gh = new GenerateHTML(site);
					String indexHtml = gh.getGeneratePcIndexHtml();
					if(Template.isAnnoConfigById_Have(indexHtml, sc.getId())){
						//判断是独立页面，还是列表页面
						if(sc.getType() - SiteColumn.TYPE_PAGE == 0){
							//独立页面
							//判断，只有当是关于我们时，才会刷新首页
							if(site.getAboutUsCid() - sc.getId() == 0){
								News news = (News) sqlService.findAloneBySqlQuery("SELECT * FROM news WHERE cid = "+sc.getId(), News.class);
								NewsData newsData = (NewsData) sqlService.findById(NewsData.class, news.getId());
								IndexAboutUs.refreshIndexData(site, sc, news, newsData.getText());
							}
						}else if (sc.getType() - SiteColumn.TYPE_IMAGENEWS == 0 || sc.getType() - SiteColumn.TYPE_NEWS == 0) {
							//若存在了，需要修改，重新刷新此栏目再首页的列表模块信息
							List<News> newsList = sqlService.findBySqlQuery("SELECT * FROM news WHERE cid = "+sc.getId() + " AND status = "+News.STATUS_NORMAL+" ORDER BY id DESC LIMIT 0,10", News.class);
							IndexNews.refreshIndexData(site, sc, newsList);
						}
					}
				}
			}
			
			//删除之前传的那个icon文件
			if(!(oldIconName == null || oldIconName.length() == 0)){
				if(oldIconName.indexOf("/") == -1){
					AttachmentFile.deleteObject("site/"+site.getId()+"/column_icon/"+oldIconName);
				}
			}
			
			//保存日志
			if(addColumn){
				AliyunLog.addActionLog(sc.getId(), "添加栏目："+sc.getName());
			}else{
				AliyunLog.addActionLog(sc.getId(), "修改栏目："+sc.getName());
			}
			
			return vo;
		}else{
			vo.setBaseVO(BaseVO.FAILURE, "保存失败");
			return vo;
		}
	}
	
	/**
	 * 更改栏目排序。CMS模式使用。（PC、手机模式使用js排序）
	 * @param id 栏目id
	 * @param rank 排序编号。数字越小越靠前
	 * @return
	 */
	@RequestMapping(value="updateRank${url.suffix}", method = RequestMethod.POST)
	@ResponseBody
	public BaseVO updateRank(HttpServletRequest request,
			@RequestParam(value = "id", required = false , defaultValue="0") int id,
			@RequestParam(value = "rank", required = false , defaultValue="0") int rank){
		if(id < 1){
			return error("请传入要操作的栏目编号");
		}
		SiteColumn siteColumn = sqlService.findById(SiteColumn.class, id);
		if(siteColumn == null){
			return error("要操作的栏目不存在");
		}
		if(siteColumn.getSiteid() - getSiteId() != 0){
			return error("栏目不属于您，无法操作");
		}
		
		siteColumn.setRank(rank);
		sqlService.save(siteColumn);
		
		//这个栏目改动完毕后，要重新将此栏目加入Session缓存中去
		siteColumnService.updateSiteColumnByCache(siteColumn);
		//记录日志
		AliyunLog.insert(request, siteColumn.getId(), "更改栏目排序");
		
		return success();
	}
	
	/**
	 * 创建/修改栏目保存(wap/pc)
	 */
	@RequestMapping(value="saveColumn${url.suffix}", method = RequestMethod.POST)
	@ResponseBody
	public BaseVO saveColumn(SiteColumn siteColumn,HttpServletRequest request,Model model){
		Site site = getSite();
		
		//标题，名字
		String name = filter(siteColumn.getName());
		if(name == null || name.length()<1){
			return error("您要创建的导航栏目叫什么名字呢");
		}
		
		//记录是否是新增导航栏目
		boolean addSite = true;
		boolean updateName = false;		//是否有过修改名字，若修改了名字了，还要吧内容页面的也一块修改
		SiteColumn sc = null;
		if(siteColumn.getId() != null && siteColumn.getId() > 0){
			addSite = false;
			sc = sqlService.findById(SiteColumn.class, siteColumn.getId());
			//修改，则需要判断一下，修改的栏目是否是自己的网站的
			if(sc.getSiteid() - site.getId() != 0){
				AliyunLog.insert(request, sc.getId(), "warn", "修改的栏目不是自己的，要修改的栏目名为："+sc.getName());
				return error("栏目不属于您，修改失败！您的操作系统已记录");
			}
			if(!sc.getName().equals(name)){
				updateName = true;
			}
		}else{
			sc = new SiteColumn();
			sc.setUserid(getUserId());
			sc.setSiteid(site.getId());
			if(site.getClient() - Site.CLIENT_CMS != 0){
				//wap、pc模式是只有一级栏目的，那么父栏目id肯定就是0
				sc.setParentid(0);
			}
		}
		
		sc.setName(name);
//		sc.setRank(siteColumn.getRank());	排序更改是有单独的方法进行的，不再这里。这里更改不了排序
		sc.setUrl(filter(siteColumn.getUrl()));
		sc.setUsed(siteColumn.getUsed());
		sc.setType(siteColumn.getType());
		
		//旧的栏目导航图名字,如果有值，那么便是上传了新图片了，要删除这个旧的
		String oldIconName = null;
		
		//上传图标，并进行压缩处理
		UploadFileVO upload= AttachmentFile.uploadImage("site/"+site.getId()+"/column_icon/", request, "iconFile", G.SITECOLUMN_ICON_MAXWIDTH);
		if(upload.getResult() == BaseVO.SUCCESS){
			oldIconName = sc.getIcon();
			sc.setIcon(upload.getFileName());
		}
		sqlService.save(sc);
		if(sc.getId() > 0){
			//删除之前传的那个icon文件
			if(oldIconName != null && oldIconName.indexOf("/") == -1){
				AttachmentFile.deleteObject("site/"+site.getId()+"/column_icon/"+oldIconName);
			}
			
			//保存日志
			if(addSite){
				AliyunLog.addActionLog(sc.getId(), "添加栏目："+sc.getName());
			}else{
				AliyunLog.addActionLog(sc.getId(), "修改栏目："+sc.getName());
			}
			
			//如果这个栏目是独立页面，那么判断是否有了这个独立页面，若没有，自动建立一个
			if(sc.getType() - SiteColumn.TYPE_PAGE == 0){
				siteColumnService.createNonePage(sc,site,updateName);
			}
			
			//判断当前网站的属性，是wap、pc、cms
			if(site.getClient() - Site.CLIENT_PC == 0){
				//PC模式，需要缓存栏目的id到Site表中
				Site s = sqlService.findById(Site.class, site.getId());
				String siteColumnId = siteService.getSiteColumnId(sc, s);
				if(siteColumnId != null){
					s.setColumnId(siteColumnId);
					sqlService.save(s);
					/**
					 * 要shiro缓存Site对象
					 */
					
				}
			}
			
			//判断当前网站的属性，是wap、pc ， 还是 cms
			if(site.getClient() - Site.CLIENT_CMS == 0){
				//CMS
			}else{
				// wap ， pc
				
				//生成栏目页面缓存
				if(sc.getType() == SiteColumn.TYPE_NEWS || sc.getType() == SiteColumn.TYPE_IMAGENEWS){
					newsService.generateListHtml(site, sc);
				}
				
				//更新栏目导航的js缓存数据 siteColumn.js
				List<SiteColumn> list = sqlService.findBySqlQuery("SELECT * FROM site_column WHERE siteid = "+site.getId()+" AND used = "+SiteColumn.USED_ENABLE+" ORDER BY rank ASC", SiteColumn.class);
				new com.xnx3.wangmarket.admin.cache.Site().siteColumn(list,site);
				
				//栏目排序，新增加的会放到最后一个，生成js缓存文件 siteColumnRank.js
				if(addSite){
					new com.xnx3.wangmarket.admin.cache.Site().siteColumnRankAppend(site, sc.getId());
				}
				
				if(site.getClient() - Site.CLIENT_PC == 0){
					//如果是pc模式，才会刷新首页
					
					//判断是否更改了栏目名字，若是更改了，还要看看首页是否调用了该栏目，还要修改首页的数据
					if(sc.getName().equals(siteColumn.getName())){
						//判断该栏目是否在首页出现，若出现过了，还要相应的修改首页的，该栏目的信息
						GenerateHTML gh = new GenerateHTML(site);
						String indexHtml = gh.getGeneratePcIndexHtml();
						if(Template.isAnnoConfigById_Have(indexHtml, sc.getId())){
							//判断是独立页面，还是列表页面
							if(sc.getType() - SiteColumn.TYPE_PAGE == 0){
								//独立页面
								//判断，只有当是关于我们时，才会刷新首页
								if(site.getAboutUsCid() - sc.getId() == 0){
									News news = (News) sqlService.findAloneBySqlQuery("SELECT * FROM news WHERE cid = "+sc.getId(), News.class);
									NewsData newsData = (NewsData) sqlService.findById(NewsData.class, news.getId());
									IndexAboutUs.refreshIndexData(site, sc, news, newsData.getText());
								}
							}else if (sc.getType() - SiteColumn.TYPE_IMAGENEWS == 0 || sc.getType() - SiteColumn.TYPE_NEWS == 0) {
								//若存在了，需要修改，重新刷新此栏目再首页的列表模块信息
								List<News> newsList = sqlService.findBySqlQuery("SELECT * FROM news WHERE cid = "+sc.getId() + " AND status = "+News.STATUS_NORMAL+" ORDER BY id DESC LIMIT 0,10", News.class);
								IndexNews.refreshIndexData(site, sc, newsList);
							}
						}
					}
				}
				
			}
			
			return success();
		}else{
			return error("保存失败！");
		}
	}

	/**
	 * 栏目导航排序，js缓存(主要WAP使用，pc高级模式更改栏目也可以用)
	 * @param rankString
	 */
	@RequestMapping(value="/saveRank${url.suffix}", method = RequestMethod.POST)
	@ResponseBody
	public BaseVO saveRank(HttpServletRequest request,
			@RequestParam(value = "rankString", required = false , defaultValue="") String rankString){
		BaseVO baseVO = new BaseVO();
		Site site = getSite();
		if(site == null){
			baseVO.setBaseVO(BaseVO.FAILURE, "要修改的栏目导航所属的网站不存在！");
			return baseVO;
		}
		if(site.getUserid() != getUserId()){
			baseVO.setBaseVO(BaseVO.FAILURE, "站点不属于您，无法修改！");
			return baseVO;
		}
		
		AliyunLog.addActionLog(site.getId(), "保存栏目排序");
		
		new com.xnx3.wangmarket.admin.cache.Site().siteColumnRank(site, Sql.filter(rankString));
		return new BaseVO();
	}
	
	/**
	 * 重置排序
	 */
	@RequestMapping(value="/resetRank${url.suffix}")
	public String resetRank(HttpServletRequest request,
			@RequestParam(value = "siteid", required = false , defaultValue="0") int siteid,
			Model model){
		if(siteid == 0){
			return error(model, "请传入站点编号");
		}
		Site site = sqlService.findById(Site.class, siteid);
		if(site == null){
			return error(model, "要重置栏目导航顺序的网站不存在！");
		}
		if(site.getUserid() != getUserId()){
			return error(model, "站点不属于您，无法修改！");
		}
		
		siteColumnService.resetColumnRankAndJs(site);
		
		AliyunLog.addActionLog(site.getId(), "重置栏目排序");
		
		return success(model, "重置栏目排序成功", Func.getConsoleRedirectUrl());
	}
	
	
	/**
	 * 删除栏目
	 * @param id 要删除的栏目的id
	 * @return
	 */
	@RequestMapping(value="delete${url.suffix}")
	@ResponseBody
	public BaseVO delete(@RequestParam(value = "id", required = false , defaultValue="0") int id){
		SiteColumn siteColumn =sqlService.findById(SiteColumn.class , id);
		if(siteColumn == null){
			return error("要删除的栏目不存在！");
		}
		if(siteColumn.getUserid() - getUserId() != 0){
			return error("栏目不属于你，无法删除");
		}
		
		sqlService.delete(siteColumn);
		
		//判断一下，如果是CMS模式下，还要将缓存中的栏目删除掉
		if(Func.isCMS(getSite())){
			siteColumnService.deleteSiteColumnByCache(siteColumn.getId());
		}
		
		AliyunLog.addActionLog(siteColumn.getId(), "删除栏目："+siteColumn.getName());
		
		return success();
	}
}
