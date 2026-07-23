<template>
  <div class="page">
    <div class="page-header"><h2>⚙️ 系统配置</h2></div>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">基础配置</h3>
          <el-form label-width="120px">
            <el-form-item label="系统名称"><el-input value="企业智慧招聘OA系统" /></el-form-item>
            <el-form-item label="系统Logo"><el-input value="/logo.png" /></el-form-item>
            <el-form-item label="允许用户注册">
              <el-switch :model-value="true" active-color="#67C23A" />
            </el-form-item>
            <el-form-item label="默认用户角色">
              <el-select :model-value="'CANDIDATE'" style="width:200px;">
                <el-option label="求职者" value="CANDIDATE" />
              </el-select>
            </el-form-item>
            <el-form-item label="简历大小限制"><el-input-number :model-value="10" :min="1" :max="50" /> MB</el-form-item>
            <el-form-item><el-button type="primary" icon="Check">保存配置</el-button></el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="section-card">
          <h3 class="card-title">AI服务配置</h3>
          <el-form label-width="120px">
            <el-form-item label="Mock模式">
              <el-switch :model-value="true" active-color="#E6A23C" />
              <span style="margin-left:8px;font-size:12px;color:#999;">Mock模式开启时不会调用真实AI接口</span>
            </el-form-item>
            <el-form-item label="API Base URL"><el-input value="https://api.openai.com" /></el-form-item>
            <el-form-item label="模型"><el-input value="qwen3.7-plus" /></el-form-item>
            <el-form-item label="超时时间"><el-input-number :model-value="30" /> 秒</el-form-item>
            <el-form-item><el-button type="primary" icon="Check">保存配置</el-button></el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-header h2 { margin: 0 0 16px; font-size: 22px; color: #3E2723; }
.section-card { border-radius: 12px; }
.card-title { margin: 0 0 14px; font-size: 16px; color: #3E2723; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; }
</style>
