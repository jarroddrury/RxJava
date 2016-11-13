//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/kgalligan/devel-doppl/RxJava/src/main/java/rx/subjects/PublishSubject.java
//

#include "J2ObjC_header.h"

#pragma push_macro("INCLUDE_ALL_RxSubjectsPublishSubject")
#ifdef RESTRICT_RxSubjectsPublishSubject
#define INCLUDE_ALL_RxSubjectsPublishSubject 0
#else
#define INCLUDE_ALL_RxSubjectsPublishSubject 1
#endif
#undef RESTRICT_RxSubjectsPublishSubject

#if !defined (RxSubjectsPublishSubject_) && (INCLUDE_ALL_RxSubjectsPublishSubject || defined(INCLUDE_RxSubjectsPublishSubject))
#define RxSubjectsPublishSubject_

#define RESTRICT_RxSubjectsSubject 1
#define INCLUDE_RxSubjectsSubject 1
#include "RxSubjectsSubject.h"

@class RxSubjectsPublishSubject_PublishSubjectState;

@interface RxSubjectsPublishSubject : RxSubjectsSubject {
 @public
  RxSubjectsPublishSubject_PublishSubjectState *state_;
}

#pragma mark Public

+ (RxSubjectsPublishSubject *)create;

- (NSException *)getThrowable;

- (jboolean)hasCompleted;

- (jboolean)hasObservers;

- (jboolean)hasThrowable;

- (void)onCompleted;

- (void)onErrorWithNSException:(NSException *)e;

- (void)onNextWithId:(id)v;

#pragma mark Protected

- (instancetype)initWithRxSubjectsPublishSubject_PublishSubjectState:(RxSubjectsPublishSubject_PublishSubjectState *)state;

@end

J2OBJC_EMPTY_STATIC_INIT(RxSubjectsPublishSubject)

J2OBJC_FIELD_SETTER(RxSubjectsPublishSubject, state_, RxSubjectsPublishSubject_PublishSubjectState *)

FOUNDATION_EXPORT RxSubjectsPublishSubject *RxSubjectsPublishSubject_create();

FOUNDATION_EXPORT void RxSubjectsPublishSubject_initWithRxSubjectsPublishSubject_PublishSubjectState_(RxSubjectsPublishSubject *self, RxSubjectsPublishSubject_PublishSubjectState *state);

FOUNDATION_EXPORT RxSubjectsPublishSubject *new_RxSubjectsPublishSubject_initWithRxSubjectsPublishSubject_PublishSubjectState_(RxSubjectsPublishSubject_PublishSubjectState *state) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT RxSubjectsPublishSubject *create_RxSubjectsPublishSubject_initWithRxSubjectsPublishSubject_PublishSubjectState_(RxSubjectsPublishSubject_PublishSubjectState *state);

J2OBJC_TYPE_LITERAL_HEADER(RxSubjectsPublishSubject)

#endif

#if !defined (RxSubjectsPublishSubject_PublishSubjectState_) && (INCLUDE_ALL_RxSubjectsPublishSubject || defined(INCLUDE_RxSubjectsPublishSubject_PublishSubjectState))
#define RxSubjectsPublishSubject_PublishSubjectState_

#define RESTRICT_JavaUtilConcurrentAtomicAtomicReference 1
#define INCLUDE_JavaUtilConcurrentAtomicAtomicReference 1
#include "java/util/concurrent/atomic/AtomicReference.h"

#define RESTRICT_RxObservable 1
#define INCLUDE_RxObservable_OnSubscribe 1
#include "RxObservable.h"

#define RESTRICT_RxObserver 1
#define INCLUDE_RxObserver 1
#include "RxObserver.h"

@class IOSObjectArray;
@class RxSubjectsPublishSubject_PublishSubjectProducer;
@class RxSubscriber;

@interface RxSubjectsPublishSubject_PublishSubjectState : JavaUtilConcurrentAtomicAtomicReference < RxObservable_OnSubscribe, RxObserver > {
 @public
  NSException *error_;
}

#pragma mark Public

- (instancetype)init;

- (void)callWithId:(RxSubscriber *)t;

- (IOSObjectArray *)get;

- (IOSObjectArray *)getAndSetWithId:(IOSObjectArray *)arg0;

- (void)onCompleted;

- (void)onErrorWithNSException:(NSException *)e;

- (void)onNextWithId:(id)t;

#pragma mark Package-Private

- (jboolean)addWithRxSubjectsPublishSubject_PublishSubjectProducer:(RxSubjectsPublishSubject_PublishSubjectProducer *)inner;

- (void)removeWithRxSubjectsPublishSubject_PublishSubjectProducer:(RxSubjectsPublishSubject_PublishSubjectProducer *)inner;

@end

J2OBJC_STATIC_INIT(RxSubjectsPublishSubject_PublishSubjectState)

J2OBJC_FIELD_SETTER(RxSubjectsPublishSubject_PublishSubjectState, error_, NSException *)

inline IOSObjectArray *RxSubjectsPublishSubject_PublishSubjectState_get_EMPTY();
/*! INTERNAL ONLY - Use accessor function from above. */
FOUNDATION_EXPORT IOSObjectArray *RxSubjectsPublishSubject_PublishSubjectState_EMPTY;
J2OBJC_STATIC_FIELD_OBJ_FINAL(RxSubjectsPublishSubject_PublishSubjectState, EMPTY, IOSObjectArray *)

inline IOSObjectArray *RxSubjectsPublishSubject_PublishSubjectState_get_TERMINATED();
/*! INTERNAL ONLY - Use accessor function from above. */
FOUNDATION_EXPORT IOSObjectArray *RxSubjectsPublishSubject_PublishSubjectState_TERMINATED;
J2OBJC_STATIC_FIELD_OBJ_FINAL(RxSubjectsPublishSubject_PublishSubjectState, TERMINATED, IOSObjectArray *)

FOUNDATION_EXPORT void RxSubjectsPublishSubject_PublishSubjectState_init(RxSubjectsPublishSubject_PublishSubjectState *self);

FOUNDATION_EXPORT RxSubjectsPublishSubject_PublishSubjectState *new_RxSubjectsPublishSubject_PublishSubjectState_init() NS_RETURNS_RETAINED;

FOUNDATION_EXPORT RxSubjectsPublishSubject_PublishSubjectState *create_RxSubjectsPublishSubject_PublishSubjectState_init();

J2OBJC_TYPE_LITERAL_HEADER(RxSubjectsPublishSubject_PublishSubjectState)

#endif

#if !defined (RxSubjectsPublishSubject_PublishSubjectProducer_) && (INCLUDE_ALL_RxSubjectsPublishSubject || defined(INCLUDE_RxSubjectsPublishSubject_PublishSubjectProducer))
#define RxSubjectsPublishSubject_PublishSubjectProducer_

#define RESTRICT_JavaUtilConcurrentAtomicAtomicLong 1
#define INCLUDE_JavaUtilConcurrentAtomicAtomicLong 1
#include "java/util/concurrent/atomic/AtomicLong.h"

#define RESTRICT_RxProducer 1
#define INCLUDE_RxProducer 1
#include "RxProducer.h"

#define RESTRICT_RxSubscription 1
#define INCLUDE_RxSubscription 1
#include "RxSubscription.h"

#define RESTRICT_RxObserver 1
#define INCLUDE_RxObserver 1
#include "RxObserver.h"

@class RxSubjectsPublishSubject_PublishSubjectState;
@class RxSubscriber;

@interface RxSubjectsPublishSubject_PublishSubjectProducer : JavaUtilConcurrentAtomicAtomicLong < RxProducer, RxSubscription, RxObserver > {
 @public
  RxSubjectsPublishSubject_PublishSubjectState *parent_;
  __unsafe_unretained RxSubscriber *actual_;
  jlong produced_;
}

#pragma mark Public

- (instancetype)initWithRxSubjectsPublishSubject_PublishSubjectState:(RxSubjectsPublishSubject_PublishSubjectState *)parent
                                                    withRxSubscriber:(RxSubscriber *)actual;

- (jboolean)isUnsubscribed;

- (void)onCompleted;

- (void)onErrorWithNSException:(NSException *)e;

- (void)onNextWithId:(id)t;

- (void)requestWithLong:(jlong)n;

- (void)unsubscribe;

@end

J2OBJC_EMPTY_STATIC_INIT(RxSubjectsPublishSubject_PublishSubjectProducer)

J2OBJC_FIELD_SETTER(RxSubjectsPublishSubject_PublishSubjectProducer, parent_, RxSubjectsPublishSubject_PublishSubjectState *)

FOUNDATION_EXPORT void RxSubjectsPublishSubject_PublishSubjectProducer_initWithRxSubjectsPublishSubject_PublishSubjectState_withRxSubscriber_(RxSubjectsPublishSubject_PublishSubjectProducer *self, RxSubjectsPublishSubject_PublishSubjectState *parent, RxSubscriber *actual);

FOUNDATION_EXPORT RxSubjectsPublishSubject_PublishSubjectProducer *new_RxSubjectsPublishSubject_PublishSubjectProducer_initWithRxSubjectsPublishSubject_PublishSubjectState_withRxSubscriber_(RxSubjectsPublishSubject_PublishSubjectState *parent, RxSubscriber *actual) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT RxSubjectsPublishSubject_PublishSubjectProducer *create_RxSubjectsPublishSubject_PublishSubjectProducer_initWithRxSubjectsPublishSubject_PublishSubjectState_withRxSubscriber_(RxSubjectsPublishSubject_PublishSubjectState *parent, RxSubscriber *actual);

J2OBJC_TYPE_LITERAL_HEADER(RxSubjectsPublishSubject_PublishSubjectProducer)

#endif

#pragma pop_macro("INCLUDE_ALL_RxSubjectsPublishSubject")
