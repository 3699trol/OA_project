<template>
  <div class="page">
    <div class="page-header"><h2>🔐 角色管理</h2><el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button></div>
    <el-card shadow="never" class="section-card">
      <el-table :data="roles" v-loading="loading" stripe>
        <el-table-column prop="roleCode" label="编码" width="140" />
        <el-table-column prop="roleName" label="名称" width="120">
          <template #default="{ row }"><el-tag :type="roleColor(row.roleCode)" effect="dark" round>{{ row.roleName }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="userCount" label="用户数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }"><el-switch :model-value="row.status === 1" active-color="#67C23A" size="small" /></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" link>分配权限</el-button>
            <el-button size="small" type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑/新增角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="520px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" status-icon>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码（如：HR、ADMIN）" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoleList, updateRole } from '@/api/user'

const loading = ref(false)
const roles = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  roleCode: '',
  roleName: '',
  description: '',
  status: 1
})

const rules = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码必须为大写字母和下划线', trigger: 'blur' }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 50, message: '角色名称不能超过50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 255, message: '描述不能超过255个字符', trigger: 'blur' }
  ]
}

function roleColor(c) {
  return { 'ADMIN': 'danger', 'HR': 'warning', 'INTERVIEWER': 'primary', 'CANDIDATE': 'success' }[c] || 'info'
}

async function fetchRoles() {
  loading.value = true
  try {
    const res = await getRoleList()
    roles.value = (res.data || []).map(r => ({ ...r, userCount: r.userCount || 0 }))
  } catch (e) {
    ElMessage.error('角色列表加载失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  form.id = row.id
  form.roleCode = row.roleCode
  form.roleName = row.roleName
  form.description = row.description || ''
  form.status = row.status
  dialogVisible.value = true
}

function resetForm() {
  form.id = null
  form.roleCode = ''
  form.roleName = ''
  form.description = ''
  form.status = 1
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateRole({
        id: form.id,
        roleCode: form.roleCode,
        roleName: form.roleName,
        description: form.description,
        status: form.status
      })
      ElMessage.success('角色更新成功')
    } else {
      // 新增角色功能后续实现
      ElMessage.info('新增功能开发中')
    }
    dialogVisible.value = false
    fetchRoles()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(fetchRoles)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-header h2 { margin: 0; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; margin-top: 16px; }
</style>
