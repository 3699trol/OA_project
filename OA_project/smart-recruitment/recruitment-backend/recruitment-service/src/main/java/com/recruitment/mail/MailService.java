package com.recruitment.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 邮件发送服务 —— 录用/淘汰通知等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    /**
     * 发送录用通知邮件（异步）。
     * 若提供了 customSubject / customBody，优先使用 HR 编辑的内容；
     * 否则使用默认 HTML 模板。
     */
    @Async
    public void sendHireNotification(String to, String candidateName, String jobName, String interviewTime,
                                      String customSubject, String customBody) {
        String subject = StringUtils.hasText(customSubject)
                ? customSubject
                : "【录用通知】恭喜您通过 " + jobName + " 岗位面试";
        String body = StringUtils.hasText(customBody)
                ? wrapCustomBody(customBody)
                : buildHireHtml(candidateName, jobName, interviewTime);
        doSend(to, subject, body);
    }

    /**
     * 发送未录用通知邮件（异步）。
     */
    @Async
    public void sendRejectNotification(String to, String candidateName, String jobName,
                                        String customSubject, String customBody) {
        String subject = StringUtils.hasText(customSubject)
                ? customSubject
                : "【面试结果】" + jobName + " 岗位面试反馈";
        String body = StringUtils.hasText(customBody)
                ? wrapCustomBody(customBody)
                : buildRejectHtml(candidateName, jobName);
        doSend(to, subject, body);
    }

    /**
     * 发送面试邀请邮件（异步）。
     */
    @Async
    public void sendInterviewInvitation(String to, String candidateName, String jobName,
                                         String interviewTime, String interviewerName, String address) {
        String subject = "【面试邀请】" + jobName + " 岗位面试通知";
        String body = buildInvitationHtml(candidateName, jobName, interviewTime, interviewerName, address);
        doSend(to, subject, body);
    }

    /**
     * 发送忘记密码验证码。该场景需要让调用方感知发送结果，因此使用同步发送。
     */
    public boolean sendPasswordResetCode(String to, String userName, String code, long expireMinutes) {
        String subject = "【密码重置】智能招聘系统验证码";
        String body = buildPasswordResetHtml(userName, code, expireMinutes);
        return doSend(to, subject, body);
    }

    // ──────────────────────────────────────────────
    // 内部实现
    // ──────────────────────────────────────────────

    private boolean doSend(String to, String subject, String html) {
        if (!StringUtils.hasText(to)) {
            log.warn("收件人邮箱为空，跳过邮件发送: subject={}", subject);
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("邮件发送成功: to={}, subject={}", to, subject);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败: to={}, subject={}, error={}", to, subject, e.getMessage(), e);
            return false;
        }
    }

    // ──────────────────────────────────────────────
    // HTML 模板
    // ──────────────────────────────────────────────

    /** 将 HR 自定义纯文本包裹为 HTML 邮件，保留换行 */
    private String wrapCustomBody(String plainText) {
        String escaped = plainText
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head>
                <body style="font-family:'Microsoft YaHei',Arial,sans-serif;background:#f4f6f9;padding:30px;">
                  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
                    <div style="padding:30px;line-height:1.8;color:#333;">
                      %s
                    </div>
                    <div style="color:#999;font-size:12px;padding:0 30px 24px;border-top:1px solid #eee;margin:0 30px;padding-top:16px;">
                      本邮件由智能招聘系统自动发送，如需帮助请联系 HR。
                    </div>
                  </div>
                </body></html>
                """.formatted(escaped);
    }

    private String buildHireHtml(String name, String jobName, String interviewTime) {
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head>
                <body style="font-family:'Microsoft YaHei',Arial,sans-serif;background:#f4f6f9;padding:30px;">
                  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,#4CAF50,#45a049);padding:30px;text-align:center;">
                      <h1 style="color:#fff;margin:0;font-size:24px;">🎉 录用通知</h1>
                      <p style="color:rgba(255,255,255,.85);margin:8px 0 0;font-size:14px;">Smart Recruitment</p>
                    </div>
                    <div style="padding:30px;line-height:1.8;color:#333;">
                      <p style="font-size:16px;">尊敬的 <strong>%s</strong>：</p>
                      <p>恭喜您！经过综合评估，您已成功通过 <strong style="color:#4CAF50;">%s</strong> 岗位的面试考核，我们诚挚地欢迎您加入团队。</p>
                      <div style="background:#f0f9eb;border-left:4px solid #4CAF50;padding:14px 18px;margin:18px 0;border-radius:6px;">
                        <p style="margin:0;font-size:14px;"><strong>🏢 录用岗位：</strong>%s</p>
                        <p style="margin:6px 0 0;font-size:14px;"><strong>📅 面试时间：</strong>%s</p>
                      </div>
                      <p>后续入职流程及相关材料，HR 将另行与您联系。</p>
                      <p>如有任何疑问，请通过本邮件回复或联系 HR。</p>
                      <p style="margin-top:24px;">祝您工作愉快！</p>
                      <p style="color:#999;font-size:12px;margin-top:24px;padding-top:16px;border-top:1px solid #eee;">
                        本邮件由智能招聘系统自动发送，请勿直接回复。如需帮助请联系 HR。
                      </p>
                    </div>
                  </div>
                </body></html>
                """.formatted(name, jobName, jobName, interviewTime != null ? interviewTime : "—");
    }

    private String buildRejectHtml(String name, String jobName) {
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head>
                <body style="font-family:'Microsoft YaHei',Arial,sans-serif;background:#f4f6f9;padding:30px;">
                  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,#607D8B,#455A64);padding:30px;text-align:center;">
                      <h1 style="color:#fff;margin:0;font-size:22px;">📋 面试结果通知</h1>
                      <p style="color:rgba(255,255,255,.85);margin:8px 0 0;font-size:14px;">Smart Recruitment</p>
                    </div>
                    <div style="padding:30px;line-height:1.8;color:#333;">
                      <p style="font-size:16px;">尊敬的 <strong>%s</strong>：</p>
                      <p>感谢您对 <strong>%s</strong> 岗位的关注与参与面试。</p>
                      <p>经过面试官综合评估，很遗憾本次未能与您继续推进。我们会将您的简历保留在人才库中，如有更合适的岗位机会将优先与您联系。</p>
                      <div style="background:#fdf6ec;border-left:4px solid #E6A23C;padding:14px 18px;margin:18px 0;border-radius:6px;">
                        <p style="margin:0;font-size:14px;"><strong>💡 提示：</strong>您可以继续浏览其他岗位投递，完善简历有助于提升匹配度。</p>
                      </div>
                      <p>再次感谢您的宝贵时间，祝您求职顺利！</p>
                      <p style="color:#999;font-size:12px;margin-top:24px;padding-top:16px;border-top:1px solid #eee;">
                        本邮件由智能招聘系统自动发送，请勿直接回复。如需帮助请联系 HR。
                      </p>
                    </div>
                  </div>
                </body></html>
                """.formatted(name, jobName);
    }

    private String buildInvitationHtml(String name, String jobName, String interviewTime,
                                        String interviewerName, String address) {
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head>
                <body style="font-family:'Microsoft YaHei',Arial,sans-serif;background:#f4f6f9;padding:30px;">
                  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,#409EFF,#337ecc);padding:30px;text-align:center;">
                      <h1 style="color:#fff;margin:0;font-size:24px;">📧 面试邀请</h1>
                      <p style="color:rgba(255,255,255,.85);margin:8px 0 0;font-size:14px;">Smart Recruitment</p>
                    </div>
                    <div style="padding:30px;line-height:1.8;color:#333;">
                      <p style="font-size:16px;">尊敬的 <strong>%s</strong>：</p>
                      <p>恭喜您通过 <strong style="color:#409EFF;">%s</strong> 岗位的简历初筛，现正式邀请您参加面试。</p>
                      <div style="background:#ecf5ff;border-left:4px solid #409EFF;padding:14px 18px;margin:18px 0;border-radius:6px;">
                        <p style="margin:0;font-size:14px;"><strong>📅 面试时间：</strong>%s</p>
                        <p style="margin:6px 0 0;font-size:14px;"><strong>👤 面试官：</strong>%s</p>
                        <p style="margin:6px 0 0;font-size:14px;"><strong>📍 地点/链接：</strong>%s</p>
                      </div>
                      <p>请提前做好准备，准时参加面试。如有时间冲突，请联系 HR 协调安排。</p>
                      <p style="margin-top:24px;">祝您面试顺利！</p>
                      <p style="color:#999;font-size:12px;margin-top:24px;padding-top:16px;border-top:1px solid #eee;">
                        本邮件由智能招聘系统自动发送，请勿直接回复。如需帮助请联系 HR。
                      </p>
                    </div>
                  </div>
                </body></html>
                """.formatted(name, jobName,
                interviewTime != null ? interviewTime : "—",
                interviewerName != null ? interviewerName : "—",
                address != null ? address : "—");
    }

    private String buildPasswordResetHtml(String name, String code, long expireMinutes) {
        return """
                <!DOCTYPE html>
                <html><head><meta charset="UTF-8"></head>
                <body style="font-family:'Microsoft YaHei',Arial,sans-serif;background:#f4f6f9;padding:30px;">
                  <div style="max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08);">
                    <div style="background:linear-gradient(135deg,#e76f51,#f4a261);padding:28px;text-align:center;">
                      <h1 style="color:#fff;margin:0;font-size:24px;">密码重置验证码</h1>
                      <p style="color:rgba(255,255,255,.85);margin:8px 0 0;font-size:14px;">Smart Recruitment</p>
                    </div>
                    <div style="padding:30px;line-height:1.8;color:#333;">
                      <p style="font-size:16px;">尊敬的 <strong>%s</strong>：</p>
                      <p>您正在重置智能招聘系统登录密码，请在页面中输入以下验证码：</p>
                      <div style="margin:22px 0;text-align:center;">
                        <span style="display:inline-block;letter-spacing:8px;font-size:30px;font-weight:700;color:#e76f51;background:#fff4ef;border:1px solid #ffd8c9;border-radius:10px;padding:12px 20px;">%s</span>
                      </div>
                      <p>验证码 <strong>%d 分钟</strong> 内有效。若非本人操作，请忽略本邮件，并尽快检查账号安全。</p>
                      <p style="color:#999;font-size:12px;margin-top:24px;padding-top:16px;border-top:1px solid #eee;">
                        本邮件由智能招聘系统自动发送，请勿直接回复。
                      </p>
                    </div>
                  </div>
                </body></html>
                """.formatted(StringUtils.hasText(name) ? name : "用户", code, expireMinutes);
    }
}
