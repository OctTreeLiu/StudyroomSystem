package com.studyspace.service;

import com.studyspace.entity.LongTermLease;
import com.studyspace.entity.Reservation;
import com.studyspace.mapper.LongTermLeaseMapper;
import com.studyspace.mapper.ReservationMapper;
import com.studyspace.vo.StudyTimeStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 学习时长统计服务类
 * 根据需求文档实现：用户使用时长统计功能需求说明.md
 */
@Service
public class StudyTimeStatisticsService {
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private LongTermLeaseMapper longTermLeaseMapper;
    
    @Autowired
    private com.studyspace.mapper.UserMapper userMapper;
    
    /**
     * 获取用户的学习时长统计
     */
    public StudyTimeStatisticsVO getUserStatistics(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 过去24小时
        LocalDateTime last24Hours = now.minusHours(24);
        Integer last24HoursMinutes = calculateTotalMinutes(userId, last24Hours, now);
        
        // 过去7天
        LocalDateTime last7Days = now.minusDays(7);
        Integer last7DaysMinutes = calculateTotalMinutes(userId, last7Days, now);
        
        // 过去30天
        LocalDateTime last30Days = now.minusDays(30);
        Integer last30DaysMinutes = calculateTotalMinutes(userId, last30Days, now);
        
        // 获取过去30天的每日统计
        List<StudyTimeStatisticsVO.DailyStatistics> dailyStatistics = getDailyStatistics(userId, last30Days, now);
        
        StudyTimeStatisticsVO vo = new StudyTimeStatisticsVO();
        vo.setLast24Hours(last24HoursMinutes);
        vo.setLast7Days(last7DaysMinutes);
        vo.setLast30Days(last30DaysMinutes);
        vo.setDailyStatistics(dailyStatistics);
        
        return vo;
    }
    
    /**
     * 计算用户在时间范围内的总学习时长
     */
    private Integer calculateTotalMinutes(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<StudyTimeStatisticsVO.DailyStatistics> dailyStats = getDailyStatistics(userId, startTime, endTime);
        int totalMinutes = 0;
        for (StudyTimeStatisticsVO.DailyStatistics daily : dailyStats) {
            totalMinutes += daily.getMinutes();
        }
        return totalMinutes;
    }
    
    /**
     * 获取所有用户的学习时长统计（管理员使用）
     */
    public StudyTimeStatisticsVO getAllUsersStatistics() {
        LocalDateTime now = LocalDateTime.now();
        
        // 过去24小时
        LocalDateTime last24Hours = now.minusHours(24);
        Integer last24HoursMinutes = calculateAllUsersTotalMinutes(last24Hours, now);
        
        // 过去7天
        LocalDateTime last7Days = now.minusDays(7);
        Integer last7DaysMinutes = calculateAllUsersTotalMinutes(last7Days, now);
        
        // 过去30天
        LocalDateTime last30Days = now.minusDays(30);
        Integer last30DaysMinutes = calculateAllUsersTotalMinutes(last30Days, now);
        
        // 获取过去30天的每日统计
        List<StudyTimeStatisticsVO.DailyStatistics> dailyStatistics = getDailyStatisticsForAllUsers(last30Days, now);
        
        StudyTimeStatisticsVO vo = new StudyTimeStatisticsVO();
        vo.setLast24Hours(last24HoursMinutes);
        vo.setLast7Days(last7DaysMinutes);
        vo.setLast30Days(last30DaysMinutes);
        vo.setDailyStatistics(dailyStatistics);
        
        return vo;
    }
    
    /**
     * 计算所有用户在时间范围内的总学习时长
     */
    private Integer calculateAllUsersTotalMinutes(LocalDateTime startTime, LocalDateTime endTime) {
        List<StudyTimeStatisticsVO.DailyStatistics> dailyStats = getDailyStatisticsForAllUsers(startTime, endTime);
        int totalMinutes = 0;
        for (StudyTimeStatisticsVO.DailyStatistics daily : dailyStats) {
            totalMinutes += daily.getMinutes();
        }
        return totalMinutes;
    }
    
    /**
     * 获取所有用户的学习时长统计（按时间段：daily/weekly/monthly）
     */
    public StudyTimeStatisticsVO getAllUsersStatisticsByPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        List<StudyTimeStatisticsVO.DailyStatistics> statistics;
        
        switch (period.toLowerCase()) {
            case "weekly":
                // 过去12周
                startTime = now.minusWeeks(12);
                statistics = getWeeklyStatisticsForAllUsers(startTime, now);
                break;
            case "monthly":
                // 过去12个月
                startTime = now.minusMonths(12);
                statistics = getMonthlyStatisticsForAllUsers(startTime, now);
                break;
            case "daily":
            default:
                // 过去30天
                startTime = now.minusDays(30);
                statistics = getDailyStatisticsForAllUsers(startTime, now);
                break;
        }
        
        StudyTimeStatisticsVO vo = new StudyTimeStatisticsVO();
        vo.setLast24Hours(0);
        vo.setLast7Days(0);
        vo.setLast30Days(0);
        vo.setDailyStatistics(statistics);
        
        return vo;
    }
    
    /**
     * 获取用户的每日统计
     * 根据需求文档实现：
     * 1. 如果某天有长期租赁，固定24小时
     * 2. 如果没有长期租赁，按预约订单计算（完全落在当日或部分重叠）
     * 3. 如果同时有长期租赁和预约，以长期租赁为准
     */
    private List<StudyTimeStatisticsVO.DailyStatistics> getDailyStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        // 查询用户在时间范围内的有效长期租赁（status=2，已付款生效）
        List<LongTermLease> leases = longTermLeaseMapper.selectActiveLeasesByUserIdAndDateRange(userId, startDate, endDate);
        
        // 构建有长期租赁的日期集合
        Set<String> leaseDates = new HashSet<>();
        for (LongTermLease lease : leases) {
            LocalDate leaseStart = lease.getStartDate();
            LocalDate leaseEnd = lease.getEndDate();
            
            // 计算租赁期间与查询时间范围的交集
            LocalDate actualStart = leaseStart.isAfter(startDate) ? leaseStart : startDate;
            LocalDate actualEnd = leaseEnd.isBefore(endDate) ? leaseEnd : endDate;
            
            // 生成所有有长期租赁的日期
            LocalDate current = actualStart;
            while (!current.isAfter(actualEnd)) {
                leaseDates.add(current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                current = current.plusDays(1);
            }
        }
        
        // 查询用户在时间范围内的有效预约（已付款且未取消）
        List<Reservation> reservations = reservationMapper.selectValidReservationsByUserIdAndDateRange(userId, startDate, endDate);
        
        // 构建日期到时长的映射（按日期拆分预约）
        Map<String, Integer> dateToMinutes = new HashMap<>();
        for (Reservation reservation : reservations) {
            if (reservation.getStartTime() == null || reservation.getEndTime() == null) {
                continue;
            }
            
            LocalDateTime reservationStart = reservation.getStartTime();
            LocalDateTime reservationEnd = reservation.getEndTime();
            
            // 如果预约跨天，需要按日期拆分
            LocalDate reservationStartDate = reservationStart.toLocalDate();
            LocalDate reservationEndDate = reservationEnd.toLocalDate();
            
            if (reservationStartDate.equals(reservationEndDate)) {
                // 情况1：预约完全落在当日内
                String dateStr = reservationStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                // 如果该日期有长期租赁，跳过预约计算（以长期租赁为准）
                if (leaseDates.contains(dateStr)) {
                    continue;
                }
                
                LocalDateTime dayStart = reservationStartDate.atStartOfDay();
                LocalDateTime dayEnd = reservationEndDate.atTime(23, 59, 59);
                
                // 计算该日期内实际使用的时间
                LocalDateTime actualStart = reservationStart.isAfter(dayStart) ? reservationStart : dayStart;
                LocalDateTime actualEnd = reservationEnd.isBefore(dayEnd) ? reservationEnd : dayEnd;
                
                // 如果是今天，只统计到当前时间
                if (reservationStartDate.equals(today)) {
                    actualEnd = actualEnd.isAfter(now) ? now : actualEnd;
                }
                
                if (!actualStart.isAfter(actualEnd)) {
                    long minutes = java.time.Duration.between(actualStart, actualEnd).toMinutes();
                    dateToMinutes.put(dateStr, dateToMinutes.getOrDefault(dateStr, 0) + (int) minutes);
                }
            } else {
                // 情况2：预约跨天，需要拆分
                LocalDate currentDate = reservationStartDate;
                while (!currentDate.isAfter(reservationEndDate)) {
                    String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    
                    // 如果该日期有长期租赁，跳过预约计算（以长期租赁为准）
                    if (leaseDates.contains(dateStr)) {
                        currentDate = currentDate.plusDays(1);
                        continue;
                    }
                    
                    LocalDateTime dayStart = currentDate.atStartOfDay();
                    LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);
                    
                    // 计算该日期内实际使用的时间
                    // 使用公式：min(订单结束时间, 当日结束时间) - max(订单开始时间, 当日开始时间)
                    LocalDateTime actualStart = currentDate.equals(reservationStartDate) ? reservationStart : dayStart;
                    LocalDateTime actualEnd = currentDate.equals(reservationEndDate) ? reservationEnd : dayEnd;
                    
                    // 如果是今天，只统计到当前时间
                    if (currentDate.equals(today)) {
                        actualEnd = actualEnd.isAfter(now) ? now : actualEnd;
                    }
                    
                    if (!actualStart.isAfter(actualEnd)) {
                        long minutes = java.time.Duration.between(actualStart, actualEnd).toMinutes();
                        dateToMinutes.put(dateStr, dateToMinutes.getOrDefault(dateStr, 0) + (int) minutes);
                    }
                    
                    currentDate = currentDate.plusDays(1);
                }
            }
        }
        
        // 生成完整的日期列表
        List<StudyTimeStatisticsVO.DailyStatistics> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(formatter);
            Integer minutes;
            
            // 如果该日期有长期租赁，算作24小时（1440分钟）
            if (leaseDates.contains(dateStr)) {
                if (currentDate.equals(today)) {
                    // 今天有长期租赁，只统计到当前时间
                    LocalDateTime dayStart = currentDate.atStartOfDay();
                    long minutesFromStartOfDay = java.time.Duration.between(dayStart, now).toMinutes();
                    minutes = (int) minutesFromStartOfDay;
                } else {
                    // 非今天，算作24小时
                    minutes = 1440; // 24小时 = 1440分钟
                }
            } else {
                // 否则使用预约的实际使用时间
                minutes = dateToMinutes.getOrDefault(dateStr, 0);
            }
            
            StudyTimeStatisticsVO.DailyStatistics daily = new StudyTimeStatisticsVO.DailyStatistics();
            daily.setDate(dateStr);
            daily.setMinutes(minutes);
            daily.setHours(Math.round(minutes / 60.0 * 10.0) / 10.0); // 保留1位小数
            
            result.add(daily);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
    
    /**
     * 获取所有用户的每日统计
     */
    private List<StudyTimeStatisticsVO.DailyStatistics> getDailyStatisticsForAllUsers(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        // 查询时间范围内的所有有效长期租赁（status=2，已付款生效）
        List<LongTermLease> leases = longTermLeaseMapper.selectAllActiveLeasesByDateRange(startDate, endDate);
        
        // 构建每个日期有长期租赁的用户ID集合（用于排除这些用户的预约时间）
        Map<String, Set<Long>> dateToLeaseUserIds = new HashMap<>();
        // 构建每个日期有长期租赁的用户数量（用于计算长期租赁时长）
        Map<String, Integer> dateToLeaseUserCount = new HashMap<>();
        for (LongTermLease lease : leases) {
            LocalDate leaseStart = lease.getStartDate();
            LocalDate leaseEnd = lease.getEndDate();
            
            // 计算租赁期间与查询时间范围的交集
            LocalDate actualStart = leaseStart.isAfter(startDate) ? leaseStart : startDate;
            LocalDate actualEnd = leaseEnd.isBefore(endDate) ? leaseEnd : endDate;
            
            // 生成所有有长期租赁的日期
            LocalDate current = actualStart;
            while (!current.isAfter(actualEnd)) {
                String dateStr = current.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                dateToLeaseUserIds.computeIfAbsent(dateStr, k -> new HashSet<>()).add(lease.getUserId());
                dateToLeaseUserCount.put(dateStr, dateToLeaseUserCount.getOrDefault(dateStr, 0) + 1);
                current = current.plusDays(1);
            }
        }
        
        // 查询所有用户在时间范围内的有效预约（已付款且未取消）
        List<Reservation> reservations = reservationMapper.selectAllValidReservationsByDateRange(startDate, endDate);
        
        // 构建日期到时长的映射（按日期拆分预约，但排除有长期租赁的用户）
        Map<String, Integer> dateToMinutes = new HashMap<>();
        for (Reservation reservation : reservations) {
            if (reservation.getStartTime() == null || reservation.getEndTime() == null) {
                continue;
            }
            
            LocalDateTime reservationStart = reservation.getStartTime();
            LocalDateTime reservationEnd = reservation.getEndTime();
            
            // 如果预约跨天，需要按日期拆分
            LocalDate reservationStartDate = reservationStart.toLocalDate();
            LocalDate reservationEndDate = reservationEnd.toLocalDate();
            
            if (reservationStartDate.equals(reservationEndDate)) {
                // 情况1：预约完全落在当日内
                String dateStr = reservationStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                // 如果该用户在这一天有长期租赁，跳过这个预约记录（以长期租赁为准）
                Set<Long> leaseUserIds = dateToLeaseUserIds.get(dateStr);
                if (leaseUserIds != null && leaseUserIds.contains(reservation.getUserId())) {
                    continue;
                }
                
                LocalDateTime dayStart = reservationStartDate.atStartOfDay();
                LocalDateTime dayEnd = reservationEndDate.atTime(23, 59, 59);
                
                // 计算该日期内实际使用的时间
                LocalDateTime actualStart = reservationStart.isAfter(dayStart) ? reservationStart : dayStart;
                LocalDateTime actualEnd = reservationEnd.isBefore(dayEnd) ? reservationEnd : dayEnd;
                
                // 如果是今天，只统计到当前时间
                if (reservationStartDate.equals(today)) {
                    actualEnd = actualEnd.isAfter(now) ? now : actualEnd;
                }
                
                if (!actualStart.isAfter(actualEnd)) {
                    long minutes = java.time.Duration.between(actualStart, actualEnd).toMinutes();
                    dateToMinutes.put(dateStr, dateToMinutes.getOrDefault(dateStr, 0) + (int) minutes);
                }
            } else {
                // 情况2：预约跨天，需要拆分
                LocalDate currentDate = reservationStartDate;
                while (!currentDate.isAfter(reservationEndDate)) {
                    String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    
                    // 如果该用户在这一天有长期租赁，跳过这个预约记录（以长期租赁为准）
                    Set<Long> leaseUserIds = dateToLeaseUserIds.get(dateStr);
                    if (leaseUserIds != null && leaseUserIds.contains(reservation.getUserId())) {
                        currentDate = currentDate.plusDays(1);
                        continue;
                    }
                    
                    LocalDateTime dayStart = currentDate.atStartOfDay();
                    LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);
                    
                    // 计算该日期内实际使用的时间
                    // 使用公式：min(订单结束时间, 当日结束时间) - max(订单开始时间, 当日开始时间)
                    LocalDateTime actualStart = currentDate.equals(reservationStartDate) ? reservationStart : dayStart;
                    LocalDateTime actualEnd = currentDate.equals(reservationEndDate) ? reservationEnd : dayEnd;
                    
                    // 如果是今天，只统计到当前时间
                    if (currentDate.equals(today)) {
                        actualEnd = actualEnd.isAfter(now) ? now : actualEnd;
                    }
                    
                    if (!actualStart.isAfter(actualEnd)) {
                        long minutes = java.time.Duration.between(actualStart, actualEnd).toMinutes();
                        dateToMinutes.put(dateStr, dateToMinutes.getOrDefault(dateStr, 0) + (int) minutes);
                    }
                    
                    currentDate = currentDate.plusDays(1);
                }
            }
        }
        
        // 生成完整的日期列表
        List<StudyTimeStatisticsVO.DailyStatistics> result = new ArrayList<>();
        LocalDate currentDate = startDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(formatter);
            Integer reservationMinutes = dateToMinutes.getOrDefault(dateStr, 0);
            Integer leaseUserCount = dateToLeaseUserCount.getOrDefault(dateStr, 0);
            
            // 计算长期租赁的时长
            Integer leaseMinutes = 0;
            if (leaseUserCount > 0) {
                if (currentDate.equals(today)) {
                    // 今天有长期租赁，只统计到当前时间
                    LocalDateTime dayStart = currentDate.atStartOfDay();
                    long minutesFromStartOfDay = java.time.Duration.between(dayStart, now).toMinutes();
                    leaseMinutes = (int) minutesFromStartOfDay * leaseUserCount;
                } else {
                    // 非今天，每个用户24小时
                    leaseMinutes = leaseUserCount * 1440; // 每个用户24小时 = 1440分钟
                }
            }
            
            // 总时长 = 预约时长（已排除有长期租赁的用户）+ 长期租赁时长
            Integer totalMinutes = reservationMinutes + leaseMinutes;
            
            StudyTimeStatisticsVO.DailyStatistics daily = new StudyTimeStatisticsVO.DailyStatistics();
            daily.setDate(dateStr);
            daily.setMinutes(totalMinutes);
            daily.setHours(Math.round(totalMinutes / 60.0 * 10.0) / 10.0); // 保留1位小数
            
            result.add(daily);
            currentDate = currentDate.plusDays(1);
        }
        
        return result;
    }
    
    /**
     * 获取所有用户的每周统计
     */
    private List<StudyTimeStatisticsVO.DailyStatistics> getWeeklyStatisticsForAllUsers(LocalDateTime startTime, LocalDateTime endTime) {
        // 使用每日统计汇总为周统计
        List<StudyTimeStatisticsVO.DailyStatistics> dailyStats = getDailyStatisticsForAllUsers(startTime, endTime);
        
        Map<String, Integer> weekToMinutes = new HashMap<>();
        for (StudyTimeStatisticsVO.DailyStatistics daily : dailyStats) {
            LocalDate date = LocalDate.parse(daily.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int year = date.getYear();
            int week = date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
            String weekStr = String.format("%d-W%02d", year, week);
            weekToMinutes.put(weekStr, weekToMinutes.getOrDefault(weekStr, 0) + daily.getMinutes());
        }
        
        List<StudyTimeStatisticsVO.DailyStatistics> result = new ArrayList<>();
        LocalDate currentDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        
        // 找到第一个周的开始（周一）
        int dayOfWeek = currentDate.getDayOfWeek().getValue();
        if (dayOfWeek != 1) {
            currentDate = currentDate.minusDays(dayOfWeek - 1);
        }
        
        while (!currentDate.isAfter(endDate)) {
            int year = currentDate.getYear();
            int week = currentDate.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
            String weekStr = String.format("%d-W%02d", year, week);
            
            Integer minutes = weekToMinutes.getOrDefault(weekStr, 0);
            
            StudyTimeStatisticsVO.DailyStatistics weekly = new StudyTimeStatisticsVO.DailyStatistics();
            weekly.setDate(weekStr);
            weekly.setMinutes(minutes);
            weekly.setHours(Math.round(minutes / 60.0 * 10.0) / 10.0);
            
            result.add(weekly);
            currentDate = currentDate.plusWeeks(1);
            
            // 防止无限循环
            if (result.size() > 52) {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * 获取所有用户的每月统计
     */
    private List<StudyTimeStatisticsVO.DailyStatistics> getMonthlyStatisticsForAllUsers(LocalDateTime startTime, LocalDateTime endTime) {
        // 使用每日统计汇总为月统计
        List<StudyTimeStatisticsVO.DailyStatistics> dailyStats = getDailyStatisticsForAllUsers(startTime, endTime);
        
        Map<String, Integer> monthToMinutes = new HashMap<>();
        for (StudyTimeStatisticsVO.DailyStatistics daily : dailyStats) {
            String monthStr = daily.getDate().substring(0, 7); // 取前7位，即 YYYY-MM
            monthToMinutes.put(monthStr, monthToMinutes.getOrDefault(monthStr, 0) + daily.getMinutes());
        }
        
        List<StudyTimeStatisticsVO.DailyStatistics> result = new ArrayList<>();
        LocalDate currentDate = startTime.toLocalDate().withDayOfMonth(1);
        LocalDate endDate = endTime.toLocalDate();
        
        while (!currentDate.isAfter(endDate)) {
            String monthStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Integer minutes = monthToMinutes.getOrDefault(monthStr, 0);
            
            StudyTimeStatisticsVO.DailyStatistics monthly = new StudyTimeStatisticsVO.DailyStatistics();
            monthly.setDate(monthStr);
            monthly.setMinutes(minutes);
            monthly.setHours(Math.round(minutes / 60.0 * 10.0) / 10.0);
            
            result.add(monthly);
            currentDate = currentDate.plusMonths(1);
            
            // 防止无限循环
            if (result.size() > 24) {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * 获取用户过去24小时的每小时统计
     */
    public List<Map<String, Object>> getUser24HoursStatistics(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        // 检查今天是否有长期租赁
        boolean hasLeaseToday = false;
        List<LongTermLease> todayLeases = longTermLeaseMapper.selectActiveLeasesByUserIdAndDateRange(userId, today, today);
        if (todayLeases != null && !todayLeases.isEmpty()) {
            hasLeaseToday = true;
        }
        
        // 如果今天有长期租赁，所有小时都是60分钟
        if (hasLeaseToday) {
            List<Map<String, Object>> result = new ArrayList<>();
            int currentHour = now.getHour();
            for (int hour = 0; hour < 24; hour++) {
                Map<String, Object> hourData = new HashMap<>();
                hourData.put("hour", hour);
                // 如果是当前小时之后，不统计
                if (hour > currentHour) {
                    hourData.put("minutes", 0);
                } else if (hour == currentHour) {
                    // 当前小时，只统计到当前分钟
                    int currentMinute = now.getMinute();
                    hourData.put("minutes", currentMinute);
                } else {
                    hourData.put("minutes", 60); // 每小时60分钟
                }
                result.add(hourData);
            }
            return result;
        }
        
        // 否则获取每小时统计数据（来自预约使用记录）
        List<Reservation> reservations = reservationMapper.selectValidReservationsByUserIdAndDateRange(
            userId, today, today);
        
        // 构建小时到分钟的映射
        Map<Integer, Integer> hourToMinutes = new HashMap<>();
        for (Reservation reservation : reservations) {
            if (reservation.getStartTime() == null || reservation.getEndTime() == null) {
                continue;
            }
            
            LocalDateTime start = reservation.getStartTime();
            LocalDateTime end = reservation.getEndTime();
            
            // 只处理今天的数据
            if (!start.toLocalDate().equals(today) && !end.toLocalDate().equals(today)) {
                continue;
            }
            
            // 计算每个小时的分钟数
            LocalDateTime current = start.toLocalDate().equals(today) ? start : today.atStartOfDay();
            LocalDateTime endTime = end.toLocalDate().equals(today) ? end : now;
            
            while (current.isBefore(endTime) && current.toLocalDate().equals(today)) {
                int hour = current.getHour();
                LocalDateTime hourStart = current.withMinute(0).withSecond(0).withNano(0);
                LocalDateTime hourEnd = hourStart.plusHours(1);
                LocalDateTime actualStart = current.isAfter(hourStart) ? current : hourStart;
                LocalDateTime actualEnd = endTime.isBefore(hourEnd) ? endTime : hourEnd;
                
                if (!actualStart.isAfter(actualEnd)) {
                    long minutes = java.time.Duration.between(actualStart, actualEnd).toMinutes();
                    hourToMinutes.put(hour, hourToMinutes.getOrDefault(hour, 0) + (int) minutes);
                }
                
                current = hourEnd;
            }
        }
        
        // 生成完整的24小时列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", hour);
            hourData.put("minutes", hourToMinutes.getOrDefault(hour, 0));
            result.add(hourData);
        }
        
        return result;
    }
    
    /**
     * 获取用户过去7天的每日统计
     */
    public List<Map<String, Object>> getUser7DaysStatistics(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last7Days = now.minusDays(7);
        List<StudyTimeStatisticsVO.DailyStatistics> dailyStats = getDailyStatistics(userId, last7Days, now);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudyTimeStatisticsVO.DailyStatistics daily : dailyStats) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", daily.getDate());
            dayData.put("minutes", daily.getMinutes());
            result.add(dayData);
        }
        
        return result;
    }
    
    /**
     * 获取所有用户过去24小时的每小时统计（管理员）
     */
    public List<Map<String, Object>> getAllUsers24HoursStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        
        // 检查今天有多少用户有长期租赁
        int todayLeaseUserCount = 0;
        List<LongTermLease> todayLeases = longTermLeaseMapper.selectAllActiveLeasesByDateRange(today, today);
        if (todayLeases != null && !todayLeases.isEmpty()) {
            Set<Long> userIds = new HashSet<>();
            for (LongTermLease lease : todayLeases) {
                userIds.add(lease.getUserId());
            }
            todayLeaseUserCount = userIds.size();
        }
        
        // 获取每小时统计数据（来自预约使用记录）
        List<Reservation> reservations = reservationMapper.selectAllValidReservationsByDateRange(today, today);
        
        // 构建小时到分钟的映射
        Map<Integer, Integer> hourToMinutes = new HashMap<>();
        for (Reservation reservation : reservations) {
            if (reservation.getStartTime() == null || reservation.getEndTime() == null) {
                continue;
            }
            
            LocalDateTime start = reservation.getStartTime();
            LocalDateTime end = reservation.getEndTime();
            
            // 只处理今天的数据
            if (!start.toLocalDate().equals(today) && !end.toLocalDate().equals(today)) {
                continue;
            }
            
            // 计算每个小时的分钟数
            LocalDateTime current = start.toLocalDate().equals(today) ? start : today.atStartOfDay();
            LocalDateTime endTime = end.toLocalDate().equals(today) ? end : now;
            
            while (current.isBefore(endTime) && current.toLocalDate().equals(today)) {
                int hour = current.getHour();
                LocalDateTime hourStart = current.withMinute(0).withSecond(0).withNano(0);
                LocalDateTime hourEnd = hourStart.plusHours(1);
                LocalDateTime actualStart = current.isAfter(hourStart) ? current : hourStart;
                LocalDateTime actualEnd = endTime.isBefore(hourEnd) ? endTime : hourEnd;
                
                if (!actualStart.isAfter(actualEnd)) {
                    long minutes = java.time.Duration.between(actualStart, actualEnd).toMinutes();
                    hourToMinutes.put(hour, hourToMinutes.getOrDefault(hour, 0) + (int) minutes);
                }
                
                current = hourEnd;
            }
        }
        
        // 生成完整的24小时列表
        List<Map<String, Object>> result = new ArrayList<>();
        int currentHour = now.getHour();
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", hour);
            
            // 如果今天有长期租赁，每个用户每小时60分钟
            int reservationMinutes = hourToMinutes.getOrDefault(hour, 0);
            int leaseMinutes = 0;
            if (hour <= currentHour) {
                if (hour == currentHour) {
                    int currentMinute = now.getMinute();
                    leaseMinutes = currentMinute * todayLeaseUserCount;
                } else {
                    leaseMinutes = 60 * todayLeaseUserCount; // 每个用户每小时60分钟
                }
            }
            hourData.put("minutes", reservationMinutes + leaseMinutes);
            
            result.add(hourData);
        }
        
        return result;
    }
    
    /**
     * 获取所有用户过去7天的每日统计（管理员）
     */
    public List<Map<String, Object>> getAllUsers7DaysStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last7Days = now.minusDays(7);
        List<StudyTimeStatisticsVO.DailyStatistics> dailyStats = getDailyStatisticsForAllUsers(last7Days, now);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudyTimeStatisticsVO.DailyStatistics daily : dailyStats) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", daily.getDate());
            dayData.put("minutes", daily.getMinutes());
            result.add(dayData);
        }
        
        return result;
    }
    
    /**
     * 获取学习红人榜
     */
    public com.studyspace.vo.StudyTimeRankingVO getStudyTimeRanking(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime;
        LocalDate startDate;
        LocalDate endDate = now.toLocalDate();
        
        switch (period.toLowerCase()) {
            case "weekly":
                // 本周榜：从本周一的00:00:00到现在
                LocalDate today = now.toLocalDate();
                int dayOfWeek = today.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
                LocalDate monday = today.minusDays(dayOfWeek - 1); // 本周一
                startTime = monday.atStartOfDay();
                startDate = monday;
                break;
            case "monthly":
                // 本月榜：从本月1号的00:00:00到现在
                LocalDate firstDayOfMonth = now.toLocalDate().withDayOfMonth(1);
                startTime = firstDayOfMonth.atStartOfDay();
                startDate = firstDayOfMonth;
                break;
            case "daily":
            default:
                // 今日榜：从今天的00:00:00到现在
                startTime = now.toLocalDate().atStartOfDay();
                startDate = now.toLocalDate();
                break;
        }
        
        // 按用户汇总
        Map<Long, Integer> userIdToMinutes = new HashMap<>();
        // 需要从预约和长期租赁中获取用户ID
        List<Reservation> reservations = reservationMapper.selectAllValidReservationsByDateRange(startDate, endDate);
        List<LongTermLease> leases = longTermLeaseMapper.selectAllActiveLeasesByDateRange(startDate, endDate);
        
        Set<Long> allUserIds = new HashSet<>();
        for (Reservation r : reservations) {
            allUserIds.add(r.getUserId());
        }
        for (LongTermLease l : leases) {
            allUserIds.add(l.getUserId());
        }
        
        // 为每个用户计算总时长
        for (Long userId : allUserIds) {
            List<StudyTimeStatisticsVO.DailyStatistics> userDailyStats = getDailyStatistics(userId, startTime, now);
            int totalMinutes = 0;
            for (StudyTimeStatisticsVO.DailyStatistics daily : userDailyStats) {
                totalMinutes += daily.getMinutes();
            }
            userIdToMinutes.put(userId, totalMinutes);
        }
        
        // 排序并取前10名
        List<Map.Entry<Long, Integer>> sorted = new ArrayList<>(userIdToMinutes.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        com.studyspace.vo.StudyTimeRankingVO vo = new com.studyspace.vo.StudyTimeRankingVO();
        List<com.studyspace.vo.StudyTimeRankingVO.RankingItem> rankingList = new ArrayList<>();
        
        int rank = 1;
        for (int i = 0; i < Math.min(10, sorted.size()); i++) {
            Map.Entry<Long, Integer> entry = sorted.get(i);
            com.studyspace.vo.StudyTimeRankingVO.RankingItem item = new com.studyspace.vo.StudyTimeRankingVO.RankingItem();
            item.setUserId(entry.getKey());
            item.setMinutes(entry.getValue());
            item.setHours(Math.round(entry.getValue() / 60.0 * 10.0) / 10.0);
            item.setRank(rank++);
            
            // 获取用户信息
            com.studyspace.entity.User user = userMapper.selectById(entry.getKey());
            if (user != null) {
                item.setUsername(user.getUsername());
                item.setRealName(user.getRealName());
                item.setAvatarUrl(user.getAvatarUrl());
            }
            
            rankingList.add(item);
        }
        
        vo.setRankingList(rankingList);
        return vo;
    }
}
