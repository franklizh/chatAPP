package com.easychat.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.easychat.entity.dto.SocialPostPublishDTO;
import com.easychat.entity.po.*;
import com.easychat.entity.query.*;
import com.easychat.entity.vo.*;
import com.easychat.mappers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.easychat.entity.enums.PageSize;
import com.easychat.service.SocialPostService;
import com.easychat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


/**
 * 社交圈帖子表 业务接口实现
 */
@Service("socialPostService")
public class SocialPostServiceImpl implements SocialPostService {

	private static final Logger logger = LoggerFactory.getLogger(UserContactServiceImpl.class);

	@Resource
	private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

	@Resource
	private SocialPostMapper<SocialPost, SocialPostQuery> socialPostMapper;

	@Resource
	private SocialPostImgMapper<SocialPostImg, SocialPostImgQuery> socialPostImgMapper;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private SocialPostLikeMapper<SocialPostLike, SocialPostLikeQuery> socialPostLikeMapper;



	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<SocialPost> findListByParam(SocialPostQuery param) {
		return this.socialPostMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(SocialPostQuery param) {
		return this.socialPostMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<SocialPost> findListByPage(SocialPostQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<SocialPost> list = this.findListByParam(param);
		PaginationResultVO<SocialPost> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(SocialPost bean) {
		return this.socialPostMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SocialPost> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.socialPostMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SocialPost> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.socialPostMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(SocialPost bean, SocialPostQuery param) {
		StringTools.checkParam(param);
		return this.socialPostMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(SocialPostQuery param) {
		StringTools.checkParam(param);
		return this.socialPostMapper.deleteByParam(param);
	}

	/**
	 * 根据PostId获取对象
	 */
	@Override
	public SocialPost getSocialPostByPostId(Long postId) {
		return this.socialPostMapper.selectByPostId(postId);
	}

	/**
	 * 根据PostId修改
	 */
	@Override
	public Integer updateSocialPostByPostId(SocialPost bean, Long postId) {
		return this.socialPostMapper.updateByPostId(bean, postId);
	}

	/**
	 * 根据PostId删除
	 */
	@Override
	public Integer deleteSocialPostByPostId(Long postId) {
		return this.socialPostMapper.deleteByPostId(postId);
	}




	@Override
	@Transactional(rollbackFor = Exception.class)
	public void publishPost(String userId, SocialPostPublishDTO dto){

		SocialPost post =new SocialPost();
		post.setUserId(userId);
		post.setContent(dto.getContent());
		post.setVisibility(0);
		post.setLikeCount(0);
		post.setCommentCount(0);
		post.setStatus(0);
		LocalDateTime now = LocalDateTime.now();  // 当前时间
		Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
		post.setCreateTime(date);

		socialPostMapper.insert(post);

		if(!CollectionUtils.isEmpty(dto.getImages())){
			int sort=0;
			for(String img:dto.getImages()){
				SocialPostImg image=new SocialPostImg();
				image.setPostId(post.getPostId());
				image.setImgUrl(img);
				image.setSort(sort++);

				socialPostImgMapper.insert(image);
			}
		}
	}


///**
// * 使用项目自定义分页方法查询动态列表
// * @param page 当前页码
// * @param pageSize 每页大小
// * @param userId 当前用户ID
// * @return 分页结果VO
// **/
//	@Override
//	public PaginationResultVO<SocialPostVO> getFeed(String userId,Integer page,Integer pageSize){
//		// 1. 创建社交圈帖子查询参数对象
//		SocialPostQuery postQuery = new SocialPostQuery();
//
//		// 2. 设置查询条件：只查询状态为0（正常）的帖子
//		postQuery.setStatus(0);
//
//		// 3. 设置排序条件：按创建时间倒序排列
//		postQuery.setOrderBy("create_time desc");
//
//		// 4. 计算总记录数
//		Integer totalCount = socialPostMapper.selectCount(postQuery);
//
//		// 5. 创建并初始化分页对象
//		SimplePage simplePage = new SimplePage(page, totalCount, pageSize);
//
//		// 6. 将分页对象设置到查询参数中
//		postQuery.setSimplePage(simplePage);
//
//		// 7. 查询分页数据列表
//		List<SocialPost> postList = socialPostMapper.selectList(postQuery);
//
//		// 8. 将查询结果转换为VO列表
//		List<SocialPostVO> voList = buildFeedVO(postList, userId);
//
//		// 9. 创建并返回新的分页结果VO
//		return new PaginationResultVO<>(
//				totalCount,
//				simplePage.getPageSize(),
//				simplePage.getPageNo(),
//				simplePage.getPageTotal(),
//				voList
//		);
//	}

	/**
	 * 使用cursor查询动态列表,支持滚轮查看
	 * @param userId 当前用户ID
	 * @param query
	 * @return 分页结果VO
	 **/
	@Override
	public FeedResultVO getFeed(String userId, FeedQuery query){
		int limit=query.getLimit()==null?20:query.getLimit();

		SocialPostQuery postQuery=new SocialPostQuery();
		postQuery.setStatus(0);

		postQuery.setLastTime(query.getLastTime());
		postQuery.setLastPostId(query.getLastPostId());

		postQuery.setOrderBy("create_time desc,post_id desc");

		postQuery.setLimit(limit+1);

		List<SocialPost> postList=socialPostMapper.selectList(postQuery);

		boolean hasMore=postList.size()>limit;
		if(hasMore){
			postList=postList.subList(0,limit);
		}

		List<SocialPostVO>voList=buildFeedVO(postList,userId);

		FeedCursorVO cursor=null;
		if(!postList.isEmpty()){
			SocialPost last=postList.get(postList.size()-1);
			cursor=new FeedCursorVO();
			cursor.setLastTime(
					last.getCreateTime().toInstant()
							.atZone(ZoneId.systemDefault())
							.toLocalDateTime()
			);
			cursor.setLastPostId(last.getPostId());
		}

		FeedResultVO result=new FeedResultVO();
		result.setList(voList);
		result.setHasMore(hasMore);
		result.setCursor(cursor);

		return result;
	}



	/**
	 * Feed VO 组装（性能关键）
	 * @param userId 当前用户ID
	 */
	private List<SocialPostVO> buildFeedVO(List<SocialPost> posts, String userId){
		if(CollectionUtils.isEmpty(posts)){
			return Collections.emptyList();
		}

		//1.批量用户
		Set<String> userIds=posts.stream()
				.map(SocialPost::getUserId)
				.collect(Collectors.toSet());

		Set<Long>postIds=posts.stream().map(SocialPost::getPostId).collect(Collectors.toSet());

		// 2. 批量好友
		UserInfoQuery userInfoQuery=new UserInfoQuery();
		userInfoQuery.setUserIdList(new ArrayList<>(userIds));

		Map<String,UserInfo> userMap=userInfoMapper.selectList(userInfoQuery)
				.stream()
				.collect(Collectors.toMap(UserInfo::getUserId,u->u));

		// 3. 批量点赞
		// Set<Long> likedPostIds = socialPostLikeMapper.selectLikedPostIds(userId,
		// 		posts.stream().map(SocialPost::getPostId).collect(Collectors.toSet())
		// );
		SocialPostLikeQuery socialPostLikeQuery=new SocialPostLikeQuery();
		socialPostLikeQuery.setUserId(userId);

		socialPostLikeQuery.setPostIdList(
				posts.stream().map(SocialPost::getPostId).collect(Collectors.toList())
		);

		List<SocialPostLike> socialPostLikeList=socialPostLikeMapper.selectList(socialPostLikeQuery);

		Set<Long>likedPostIds=socialPostLikeList.stream().map(SocialPostLike::getPostId).collect(Collectors.toSet());


		//  好友ID
		//Set<String> friendIds = userContactMapper.selectFriendIds(userId);
		UserContactQuery userContactQuery=new UserContactQuery();
		userContactQuery.setUserId(userId);
		userContactQuery.setContactType(0);
		userContactQuery.setStatus(1);
		List<UserContact> userContactList=userContactMapper.selectList(userContactQuery);

		Set<String>friendIds=userContactList.stream()
				.map(UserContact::getContactId)
				.filter(contactId->{
					// 好友ID格式必须为U+数字
					if (!contactId.startsWith("U")) {
						return false;
					}
					// 排除特定的非用户ID
					if ("USQLrobot".equals(contactId) || "Urobot".equals(contactId)) {
						return false;
					}
					// 验证剩余部分是否为数字
					try {
						Long.parseLong(contactId.substring(1));
						return true;
					} catch (NumberFormatException e) {
						return false;
					}
				}).collect(Collectors.toSet());


		//批量查询帖子图片
		SocialPostImgQuery imgQuery=new SocialPostImgQuery();
		imgQuery.setPostIdList(new ArrayList<>(postIds));

		Map<Long,List<String>>postImagesMap=socialPostImgMapper.selectList(imgQuery)
				.stream().collect(Collectors.groupingBy(
						SocialPostImg::getPostId,
						Collectors.mapping(
								SocialPostImg::getImgUrl,
								Collectors.toList()
						)
				));



		return posts.stream().map(post->{
			SocialPostVO vo=new SocialPostVO();
			vo.setPostId(post.getPostId());
			vo.setContent(post.getContent());
			//vo.setCreateTime(post.getCreateTime());
			LocalDateTime createTime = post.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			vo.setCreateTime(createTime);

			UserInfo user=userMap.get(post.getUserId());
			UserSimpleVO userVO=new UserSimpleVO();
			userVO.setUserId(user.getUserId());
			userVO.setNickName(user.getNickName());
			//userVO.setAvatar(user.getAvatar);

			vo.setUser(userVO);
			vo.setIsFriend(friendIds.contains(post.getUserId()));
			vo.setIsLiked(likedPostIds.contains(post.getPostId()));

			vo.setImages(
					postImagesMap.getOrDefault(post.getPostId(), Collections.emptyList())
			);

			return vo;
		}).collect(Collectors.toList());
	}
}