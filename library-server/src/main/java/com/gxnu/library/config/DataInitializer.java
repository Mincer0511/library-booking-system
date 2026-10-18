package com.gxnu.library.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxnu.library.common.constant.ConfigKey;
import com.gxnu.library.common.constant.RoleKey;
import com.gxnu.library.entity.LibraryFloor;
import com.gxnu.library.entity.StudyRoom;
import com.gxnu.library.entity.StudySeat;
import com.gxnu.library.entity.SysConfig;
import com.gxnu.library.entity.SysNotice;
import com.gxnu.library.entity.SysRole;
import com.gxnu.library.entity.SysUser;
import com.gxnu.library.mapper.LibraryFloorMapper;
import com.gxnu.library.mapper.StudyRoomMapper;
import com.gxnu.library.mapper.StudySeatMapper;
import com.gxnu.library.mapper.SysConfigMapper;
import com.gxnu.library.mapper.SysNoticeMapper;
import com.gxnu.library.mapper.SysRoleMapper;
import com.gxnu.library.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始数据装配器：首次启动时写入角色、楼层、系统配置、测试账号、示例自习室与座位、示例公告。
 * 全部为幂等操作（存在则跳过），可随应用反复启动。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final LibraryFloorMapper floorMapper;
    private final SysConfigMapper configMapper;
    private final StudyRoomMapper roomMapper;
    private final StudySeatMapper seatMapper;
    private final SysNoticeMapper noticeMapper;

    @Override
    public void run(String... args) {
        seedRoles();
        seedFloors();
        seedConfigs();
        seedUsers();
        seedRoomsAndSeats();
        seedNotices();
    }

    /** 角色：学生 / 图书馆管理员 / 超级管理员 */
    private void seedRoles() {
        if (roleMapper.selectCount(null) > 0) {
            return;
        }
        insertRole("学生", RoleKey.STUDENT, "在校学生，预约使用自习室");
        insertRole("图书馆管理员", RoleKey.LIBRARIAN, "自习室运营与日常管理");
        insertRole("超级管理员", RoleKey.SUPER_ADMIN, "系统配置与全量管理");
        log.info("[初始化] 角色数据写入完成");
    }

    private void insertRole(String name, String key, String desc) {
        SysRole role = new SysRole();
        role.setRoleName(name);
        role.setRoleKey(key);
        role.setDescription(desc);
        roleMapper.insert(role);
    }

    /** 楼层：1-5层 */
    private void seedFloors() {
        if (floorMapper.selectCount(null) > 0) {
            return;
        }
        String[] names = {"一层", "二层", "三层", "四层", "五层"};
        for (int i = 0; i < 5; i++) {
            LibraryFloor floor = new LibraryFloor();
            floor.setFloorNum(i + 1);
            floor.setFloorName(names[i]);
            floor.setDescription("图书馆" + names[i] + "自习区域");
            floorMapper.insert(floor);
        }
        log.info("[初始化] 楼层数据写入完成");
    }

    /** 系统配置：预约与信用分规则 */
    private void seedConfigs() {
        Map<String, String[]> configs = new LinkedHashMap<>();
        configs.put(ConfigKey.BOOKING_MAX_PER_DAY, new String[]{"2", "每日最大预约单数"});
        configs.put(ConfigKey.BOOKING_MAX_HOURS, new String[]{"4", "单次最长预约时长（小时）"});
        configs.put(ConfigKey.BOOKING_ADVANCE_DAYS, new String[]{"1", "可提前预约天数（0仅当天 1今天+明天）"});
        configs.put(ConfigKey.CHECKIN_LATE_MINUTES, new String[]{"30", "超时未签到判爽约分钟数"});
        configs.put(ConfigKey.CREDIT_INIT_SCORE, new String[]{"100", "初始信用分"});
        configs.put(ConfigKey.CREDIT_VIOLATION_DEDUCT, new String[]{"10", "爽约单次扣分"});
        configs.put(ConfigKey.CREDIT_DISABLE_THRESHOLD, new String[]{"60", "禁用阈值（低于该值禁用）"});
        configs.put(ConfigKey.CREDIT_DISABLE_DAYS, new String[]{"7", "禁用天数"});
        configs.put(ConfigKey.CREDIT_RESTORE_SCORE, new String[]{"60", "解禁后恢复分数"});
        configs.forEach((key, val) -> {
            Long count = configMapper.selectCount(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
            if (count == 0) {
                SysConfig config = new SysConfig();
                config.setConfigKey(key);
                config.setConfigValue(val[0]);
                config.setDescription(val[1]);
                configMapper.insert(config);
            }
        });
        log.info("[初始化] 系统配置写入完成");
    }

    /** 测试账号（密码均为 123456，BCrypt 加密） */
    private void seedUsers() {
        if (userMapper.selectCount(null) > 0) {
            return;
        }
        Long studentRoleId = roleIdOf(RoleKey.STUDENT);
        Long librarianRoleId = roleIdOf(RoleKey.LIBRARIAN);
        Long superAdminRoleId = roleIdOf(RoleKey.SUPER_ADMIN);

        insertUser("20230001", "张同学", "13800000001", studentRoleId, 100);
        insertUser("20230002", "李同学", "13800000002", studentRoleId, 90);
        insertUser("admin", "馆员老师", "13800000003", librarianRoleId, 100);
        insertUser("superadmin", "系统管理员", "13800000004", superAdminRoleId, 100);
        log.info("[初始化] 测试账号写入完成（密码均为 123456）");
    }

    private Long roleIdOf(String roleKey) {
        return roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, roleKey)).getRoleId();
    }

    private void insertUser(String username, String name, String phone, Long roleId, int credit) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw("123456"));
        user.setName(name);
        user.setNickname(name);
        user.setPhone(phone);
        user.setRoleId(roleId);
        user.setCreditScore(credit);
        user.setIsDisabled(0);
        user.setStatus(1);
        userMapper.insert(user);
    }

    /** 示例自习室与座位：每层 2 间，每间 4 行 × 6 列 = 24 个座位 */
    private void seedRoomsAndSeats() {
        if (roomMapper.selectCount(null) > 0) {
            return;
        }
        List<LibraryFloor> floors = floorMapper.selectList(
                new LambdaQueryWrapper<LibraryFloor>().orderByAsc(LibraryFloor::getFloorNum));
        String[] areas = {"A", "B"};
        String[] types = {"普通", "静音"};
        for (LibraryFloor floor : floors) {
            for (int i = 0; i < areas.length; i++) {
                StudyRoom room = new StudyRoom();
                room.setFloorId(floor.getFloorId());
                room.setRoomName(floor.getFloorNum() + "F-" + areas[i] + "自习室");
                room.setOpenTime(LocalTime.of(7, 30));
                room.setCloseTime(LocalTime.of(22, 30));
                room.setRoomType(types[i]);
                room.setDescription(floor.getFloorName() + areas[i] + "区自习室，环境安静，适合沉浸式学习");
                room.setStatus(1);
                roomMapper.insert(room);
                seedSeats(room);
            }
        }
        log.info("[初始化] 示例自习室与座位写入完成");
    }

    /** 单房间座位：行 A-D（y=1..4），列 1-6（x=1..6）；两侧靠窗，首末排带插座 */
    private void seedSeats(StudyRoom room) {
        String[] rowLetters = {"A", "B", "C", "D"};
        int count = 0;
        for (int y = 1; y <= 4; y++) {
            for (int x = 1; x <= 6; x++) {
                StudySeat seat = new StudySeat();
                seat.setRoomId(room.getRoomId());
                seat.setSeatCode(rowLetters[y - 1] + String.format("%02d", x));
                boolean window = (x == 1 || x == 6);
                boolean socket = (y == 1 || y == 4);
                seat.setSeatType(window ? "靠窗" : (socket ? "带插座" : "普通"));
                seat.setHasSocket(socket ? 1 : 0);
                seat.setPositionX(x);
                seat.setPositionY(y);
                seat.setStatus(1);
                seatMapper.insert(seat);
                count++;
            }
        }
        room.setSeatCount(count);
        roomMapper.updateById(room);
    }

    /** 示例公告 */
    private void seedNotices() {
        if (noticeMapper.selectCount(null) > 0) {
            return;
        }
        insertNotice("罗文校区图书馆自习室预约系统上线啦", 
                "同学们好！图书馆1-5层自习室现已支持线上预约座位，" +
                "请文明预约、按时签到签退，共同维护良好的自习环境。", 1);
        insertNotice("预约与信用分规则说明", 
                "每日最多预约2次，单次最长4小时；预约开始后30分钟内请完成签到，" +
                "超时未签到记爽约并扣信用分10分；信用分低于60分将禁用预约7天。", 0);
        log.info("[初始化] 示例公告写入完成");
    }

    private void insertNotice(String title, String content, int isTop) {
        SysNotice notice = new SysNotice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setIsTop(isTop);
        notice.setStatus(1);
        noticeMapper.insert(notice);
    }
}
