/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.scheduler.impl;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.scheduler.CronTrigger;
import org.bonitasoft.engine.scheduler.JobIdentifier;
import org.bonitasoft.engine.scheduler.RepeatTrigger;
import org.bonitasoft.engine.scheduler.SSchedulerException;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.Trigger;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.TenantIdNotSetException;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.TransactionState;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.core.QuartzScheduler;
import org.quartz.impl.matchers.GroupMatcher;

/**
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 */
public class QuartzSchedulerExecutor implements SchedulerExecutor {

    private Scheduler scheduler;

    private final BonitaSchedulerFactory schedulerFactory;

    private final ReadSessionAccessor sessionAccessor;

    private final TransactionService transactionService;

    private final boolean useOptimization;

    private Field quartzSchedulerField;

    private QuartzScheduler quartzScheduler;

    public QuartzSchedulerExecutor(final BonitaSchedulerFactory schedulerFactory, final ReadSessionAccessor sessionAccessor,
            final TransactionService transactionService, final boolean useOptimization) {
        this.sessionAccessor = sessionAccessor;
        this.schedulerFactory = schedulerFactory;
        this.transactionService = transactionService;
        this.useOptimization = useOptimization;
    }

    @Override
    public void schedule(final JobIdentifier jobIdentifier, final Trigger trigger) throws SSchedulerException {
        try {
            checkSchedulerState();
            final JobDetail jobDetail = getJobDetail(jobIdentifier);
            final JobKey jobKey = jobDetail.getKey();
            final org.quartz.Trigger quartzTrigger = getQuartzTrigger(trigger, jobKey.getName(), jobKey.getGroup());
            scheduler.scheduleJob(jobDetail, quartzTrigger);
            if (useOptimization) {
                transactionService.registerBonitaSynchronization(new NotifyQuartzOfNewTrigger(trigger.getStartDate().getTime(), quartzScheduler));
            }
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    private final class NotifyQuartzOfNewTrigger implements BonitaTransactionSynchronization {

        private final long time;

        private final QuartzScheduler quartzScheduler;

        public NotifyQuartzOfNewTrigger(final long time, final QuartzScheduler quartzScheduler) {
            super();
            this.time = time;
            this.quartzScheduler = quartzScheduler;
        }

        @Override
        public void beforeCommit() {
            // NOTHING
        }

        @Override
        public void afterCompletion(final TransactionState txState) {
            if (TransactionState.COMMITTED.equals(txState)) {
                if (quartzScheduler != null) {
                    quartzScheduler.getSchedulerSignaler().signalSchedulingChange(time);
                }
            }
        }
    }

    private JobDetail getJobDetail(final JobIdentifier jobIdentifier) {
        final String jobName = jobIdentifier.getJobName();
        final String tenantId = String.valueOf(jobIdentifier.getTenantId());
        final JobDetail jobDetail = JobBuilder.newJob(QuartzJob.class).withIdentity(jobName, tenantId).build();
        jobDetail.getJobDataMap().put("jobIdentifier", jobIdentifier);
        return jobDetail;
    }

    @Override
    public void executeNow(final JobIdentifier jobIdentifier) throws SSchedulerException {
        try {
            checkSchedulerState();
            final JobDetail jobDetail = getJobDetail(jobIdentifier);
            scheduler.addJob(jobDetail, true);
            scheduler.triggerJob(jobDetail.getKey());
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    private org.quartz.Trigger getQuartzTrigger(final Trigger trigger, final String jobName, final String tenantId) throws ParseException, SSchedulerException {
        final TriggerBuilder<? extends org.quartz.Trigger> triggerBuilder;
        final TriggerBuilder<org.quartz.Trigger> base = TriggerBuilder.newTrigger().forJob(jobName, tenantId).withIdentity(trigger.getName(), tenantId)
                .startNow();
        if (trigger instanceof CronTrigger) {
            final CronTrigger cronTrigger = (CronTrigger) trigger;
            final CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronTrigger.getExpression());
            triggerBuilder = base.withSchedule(cronScheduleBuilder).endAt(cronTrigger.getEndDate());
        } else if (trigger instanceof RepeatTrigger) {
            final RepeatTrigger repeatTrigger = (RepeatTrigger) trigger;
            final SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(repeatTrigger.getInterval())
                    .withRepeatCount(repeatTrigger.getCount());
            triggerBuilder = base.withSchedule(scheduleBuilder).startAt(repeatTrigger.getStartDate());
        } else {
            triggerBuilder = base.startAt(trigger.getStartDate());
        }
        return triggerBuilder.withPriority(trigger.getPriority()).build();
    }

    @Override
    public boolean isStarted() throws SSchedulerException {
        try {
            return scheduler != null && scheduler.isStarted() && !scheduler.isShutdown();
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public boolean isShutdown() throws SSchedulerException {
        try {
            return scheduler != null && scheduler.isShutdown();
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void start() throws SSchedulerException {
        try {
            if (!isShutdown()) {
                if (isStarted()) {
                    throw new SSchedulerException("The scheduler is already started.");
                }
                // shutdown();
            }
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();

            try {
                if (useOptimization) {
                    quartzSchedulerField = scheduler.getClass().getDeclaredField("sched");
                    quartzSchedulerField.setAccessible(true);
                    quartzScheduler = (QuartzScheduler) quartzSchedulerField.get(scheduler);
                }
            } catch (Throwable t) {
                // this is an optimization, we do not want it to make the system failing
                t.printStackTrace();
            }

        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void shutdown() throws SSchedulerException {
        try {
            checkSchedulerState();
            scheduler.shutdown(true);
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    private void checkSchedulerState() throws SSchedulerException {
        if (scheduler == null) {
            throw new SSchedulerException("The scheduler is not started");
        }
    }

    @Override
    public void reschedule(final String triggerName, final Trigger newTrigger) throws SSchedulerException {
        try {
            final String tenantId = String.valueOf(getTenantIdFromSession());
            if (triggerName == null) {
                throw new SSchedulerException("The trigger name is null");
            } else if (tenantId == null) {
                throw new SSchedulerException("The trigger group name is null");
            }
            checkSchedulerState();
            final org.quartz.Trigger quartzTrigger = getQuartzTrigger(newTrigger, triggerName, tenantId);
            final org.quartz.Trigger trigger = scheduler.getTrigger(triggerKey(triggerName, tenantId));
            if (trigger == null) {
                throw new SSchedulerException("No trigger found with name: " + triggerName + " and tenant: " + tenantId);
            }
            scheduler.rescheduleJob(trigger.getKey(), quartzTrigger);
        } catch (final Exception e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void resume(final String jobName) throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            scheduler.resumeJob(jobKey(jobName, tenantId));
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void resumeJobs() throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            final GroupMatcher<JobKey> jobGroupEquals = jobGroupEquals(tenantId);
            scheduler.resumeJobs(jobGroupEquals);
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void pause(final String jobName) throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            scheduler.pauseJob(jobKey(jobName, tenantId));
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void pauseJobs() throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            scheduler.pauseJobs(jobGroupEquals(tenantId));
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public boolean delete(final String jobName) throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            return scheduler.deleteJob(jobKey(jobName, tenantId));
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void deleteJobs() throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            final Set<JobKey> jobNames = scheduler.getJobKeys(jobGroupEquals(tenantId));
            for (final JobKey jobKey : jobNames) {
                delete(jobKey.getName());
            }
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public List<String> getJobs() throws SSchedulerException {
        try {
            checkSchedulerState();
            final String tenantId = String.valueOf(getTenantIdFromSession());
            final Set<JobKey> jobKeys = scheduler.getJobKeys(jobGroupEquals(tenantId));
            final List<String> jobsNames = new ArrayList<String>(jobKeys.size());
            for (final JobKey jobKey : jobKeys) {
                jobsNames.add(jobKey.getName());
            }
            return jobsNames;
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        } catch (final TenantIdNotSetException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public List<String> getAllJobs() throws SSchedulerException {
        try {
            checkSchedulerState();
            final Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupStartsWith(""));
            final List<String> jobsNames = new ArrayList<String>(jobKeys.size());
            for (final JobKey jobKey : jobKeys) {
                jobsNames.add(jobKey.getName());
            }
            return jobsNames;
        } catch (final org.quartz.SchedulerException e) {
            throw new SSchedulerException(e);
        }
    }

    @Override
    public void setBOSSchedulerService(final SchedulerImpl schedulerService) {
        schedulerFactory.setBOSSchedulerService(schedulerService);
    }

    private long getTenantIdFromSession() throws TenantIdNotSetException {
        return sessionAccessor.getTenantId();
    }

}
