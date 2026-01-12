<template>
  <Layout>
    <template #left-content>
      <div class="drag-panel drag"></div>
      <div class="top-search">
        <el-input placeholder="搜索" v-model="searchKey" size="small">
          <template #suffix>
            <span class="iconfont icon-search"></span>
          </template>
        </el-input>
      </div>
      <!-- 搜索栏下面区域置空，准备后续开发 -->
    </template>
    <template #right-content>
      <div class="social-container">
        <div class="social-header">
          <h1>社交圈</h1>
        </div>
        <div class="social-tabs">
          <div 
            :class="['tab', { active: activeTab === 'friends' }]" 
            @click="activeTab = 'friends'"
          >
            朋友
          </div>
          <div 
            :class="['tab', { active: activeTab === 'feed' }]" 
            @click="activeTab = 'feed'"
          >
            动态
          </div>
          <div 
            :class="['tab', { active: activeTab === 'me' }]" 
            @click="activeTab = 'me'"
          >
            我的
          </div>
        </div>
        <div class="social-content" @scroll="handleScroll">
          <div v-if="activeTab === 'feed'" class="feed-container">
            <div class="post-item" 
              v-for="post in postList" 
              :key="post.postId"
            >
              <div class="post-header">
                <Avatar :userId="post.user?.userId" :width="40" :showDetail="false"></Avatar>
                <div class="post-user-info">
                  <div class="user-name">{{ post.user?.nickName }}</div>
                  <div class="post-time">{{ formatTime(post.createTime) }}</div>
                </div>
              </div>
              <div class="post-content">
                {{ post.content }}
              </div>
              <!-- 图片展示 -->
               <div v-if="resolveImages(post.images).length" class="post-images">
                <div v-for="(url, index) in resolveImages(post.images)" :key="index" class="post-image-item">
                    <img
                      :src="url"
                      :alt="`帖子图片 ${index + 1}`"
                      class="post-image"
                    />
                </div>
              </div>

              <div class="post-actions">
                <div class="action-item">
                  <span class="iconfont icon-comment"></span>
                  <span>{{ post.commentCount || 0 }}</span>
                </div>
                <div class="action-item" :class="{ 'liked': post.isLiked }">
                  <span class="iconfont icon-like"></span>
                  <span>{{ post.likeCount || 0 }}</span>
                </div>
              </div>
            </div>
            <div v-if="loading" class="loading">
            <img src="@/assets/img/loading.gif" alt="加载中">
          </div>
          <div v-if="!loading && !hasMore && postList.length > 0" class="no-more">
            没有更多内容了
          </div>
          </div>
        </div>
      </div>
    </template>
  </Layout>
</template>

<script setup>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { useUserInfoStore } from '@/stores/UserInfoStore'
import Avatar from '@/components/Avatar.vue'
import Layout from '@/components/Layout.vue'

const { proxy } = getCurrentInstance()
const userInfoStore = useUserInfoStore()
const activeTab = ref('feed')
const postList = ref([])
const loading = ref(false)
const limit = ref(20)
const hasMore = ref(true)
const cursor = ref({
  lastTime: null,
  lastPostId: null
})
const searchKey = ref('')

const getFeedList = async () => {
  if (loading.value || !hasMore.value) return
  loading.value = true

  try {
    const params = { limit: limit.value }

    if (cursor.value.lastTime && cursor.value.lastPostId) {
      params.lastTime = cursor.value.lastTime
      params.lastPostId = cursor.value.lastPostId
    }

    const res = await proxy.Request({
      url: proxy.Api.socialFeed,
      params,
      method: 'get'
    })

    console.log('feed res = ', res)

    // ✅ 正确解包 ResponseVO
    const feedData = res?.data
    if (!feedData) {
      hasMore.value = false
      return
    }

    if (feedData.list && feedData.list.length > 0) {
      postList.value = cursor.value.lastTime
        ? postList.value.concat(feedData.list)
        : feedData.list

      cursor.value = feedData.cursor
      hasMore.value = feedData.hasMore
    } else {
      hasMore.value = false
    }

  } catch (e) {
    console.error('getFeedList error', e)
  } finally {
    loading.value = false
  }
}


// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

// 下拉加载更多
const handleScroll = (e) => {
  const target = e.target
  const scrollTop = target.scrollTop
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight
  
  if (scrollTop + clientHeight >= scrollHeight - 100 && !loading.value && hasMore.value) {
    getFeedList()
  }
}

const resolveImages = (images) => {
  if (!Array.isArray(images)) return []

  return images
    .map(getImageUrl)
    .filter(Boolean)
}


const ALLOWED_IMAGE_EXT = /\.(png|jpe?g|gif|webp|bmp)(\?.*)?$/i

const getImageUrl = (imagePath) => {
  if (typeof imagePath !== 'string') return ''

  const path = imagePath.trim()
  if (!path) return ''

  // ✅ 允许相对路径（交给后端 / nginx）
  if (path.startsWith('/')) {
    return path
  }

  // http(s)
  if (!/^https?:\/\//i.test(path)) return ''

  // 后缀校验（支持 ?）
  if (!ALLOWED_IMAGE_EXT.test(path)) return ''

  return path
}




// 检查是否有有效的图片
const hasValidImages = (post) => {
  if (!post.images || !Array.isArray(post.images)) {
    return false
  }
  // 过滤掉空字符串和无效图片URL
  return post.images.some(image => {
    const imageUrl = getImageUrl(image)
    return imageUrl && imageUrl.trim() !== ''
  })
}




onMounted(() => {
  getFeedList()
})
</script>

<style lang="scss" scoped>
    /* 搜索栏样式 */
.top-search {
  padding: 10px;
  .el-input {
    width: 100%;
    .el-input__wrapper {
      border-radius: 20px;
    }
  }
}

/* 拖拽面板样式 */
.drag-panel {
  width: 100%;
  height: 30px;
  user-select: none;
}

.social-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #fff;
}

.social-header {
  padding: 20px;
  border-bottom: 1px solid #e6e6e6;
  background-color: #fff;
  h1 {
    margin: 0;
    font-size: 24px;
    font-weight: bold;
  }
}

.social-tabs {
  display: flex;
  border-bottom: 1px solid #e6e6e6;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  
  .tab {
    flex: 1;
    text-align: center;
    padding: 15px 0;
    cursor: pointer;
    font-size: 16px;
    color: #666;
    transition: all 0.3s;
    
    &:hover {
      background-color: #f5f5f5;
    }
    
    &.active {
      color: #1da1f2;
      font-weight: bold;
      border-bottom: 2px solid #1da1f2;
    }
  }
}

.social-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  background-color: #f5f5f5;
  min-height: 0;
  
  .feed-container {
    max-width: 800px;
    margin: 0 auto;
    
    .post-item {
      background-color: #fff;
      border: none;
      border-bottom: 1px solid #e6e6e6;
      border-radius: 0;
      padding: 15px 20px;
      margin-bottom: 0;
      box-shadow: none;
      
      .post-header {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        
        .post-user-info {
          margin-left: 10px;
          
          .user-name {
            font-weight: bold;
            margin-bottom: 2px;
          }
          
          .post-time {
            font-size: 12px;
            color: #999;
          }
        }
      }
      
      .post-content {
        margin-bottom: 15px;
        line-height: 1.6;
        white-space: pre-wrap;
      }
      
      /* 图片样式 */
      .post-images {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 15px;
        
        .post-image-item {
          flex: 0 0 calc(33.333% - 5.333px);
          max-width: calc(33.333% - 5.333px);
          
          .post-image {
            width: 100%;
            height: auto;
            border-radius: 8px;
            object-fit: cover;
          }
        }
      }
      
      .post-actions {
        display: flex;
        justify-content: flex-start;
        
        .action-item {
          display: flex;
          align-items: center;
          margin-right: 50px;
          cursor: pointer;
          color: #666;
          transition: color 0.3s;
          font-size: 14px;
          
          .iconfont {
            margin-right: 8px;
            font-size: 18px;
          }
          
          &:hover {
            color: #1da1f2;
          }
          
          /* 已点赞样式 */
          &.liked {
            color: #e0245e;
          }
        }
      }
    }
    
    .loading {
      text-align: center;
      padding: 20px;
      background-color: #f5f5f5;
      
      img {
        width: 40px;
        height: 40px;
      }
    }
    
    .no-more {
      text-align: center;
      padding: 20px;
      color: #999;
      font-size: 14px;
      background-color: #f5f5f5;
    }
  }
}
</style>
