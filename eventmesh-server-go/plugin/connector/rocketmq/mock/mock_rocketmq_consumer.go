// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Code generated by MockGen. DO NOT EDIT.
// Source: /Users/chenzhou/horoc/incubator-eventmesh/eventmesh-server-go/plugin/connector/rocketmq/client/rocketmq_consumer.go

// Package mock is a generated GoMock package.
package mock

import (
	reflect "reflect"

	client "github.com/apache/incubator-eventmesh/eventmesh-server-go/plugin/connector/rocketmq/client"
	consumer "github.com/apache/rocketmq-client-go/v2/consumer"
	gomock "github.com/golang/mock/gomock"
)

// MockRocketMQConsumer is a mock of RocketMQConsumer interface.
type MockRocketMQConsumer struct {
	ctrl     *gomock.Controller
	recorder *MockRocketMQConsumerMockRecorder
}

// MockRocketMQConsumerMockRecorder is the mock recorder for MockRocketMQConsumer.
type MockRocketMQConsumerMockRecorder struct {
	mock *MockRocketMQConsumer
}

// NewMockRocketMQConsumer creates a new mock instance.
func NewMockRocketMQConsumer(ctrl *gomock.Controller) *MockRocketMQConsumer {
	mock := &MockRocketMQConsumer{ctrl: ctrl}
	mock.recorder = &MockRocketMQConsumerMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockRocketMQConsumer) EXPECT() *MockRocketMQConsumerMockRecorder {
	return m.recorder
}

// IsBroadCasting mocks base method.
func (m *MockRocketMQConsumer) IsBroadCasting() bool {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "IsBroadCasting")
	ret0, _ := ret[0].(bool)
	return ret0
}

// IsBroadCasting indicates an expected call of IsBroadCasting.
func (mr *MockRocketMQConsumerMockRecorder) IsBroadCasting() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "IsBroadCasting", reflect.TypeOf((*MockRocketMQConsumer)(nil).IsBroadCasting))
}

// Resume mocks base method.
func (m *MockRocketMQConsumer) Resume() {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "Resume")
}

// Resume indicates an expected call of Resume.
func (mr *MockRocketMQConsumerMockRecorder) Resume() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Resume", reflect.TypeOf((*MockRocketMQConsumer)(nil).Resume))
}

// Shutdown mocks base method.
func (m *MockRocketMQConsumer) Shutdown() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Shutdown")
	ret0, _ := ret[0].(error)
	return ret0
}

// Shutdown indicates an expected call of Shutdown.
func (mr *MockRocketMQConsumerMockRecorder) Shutdown() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Shutdown", reflect.TypeOf((*MockRocketMQConsumer)(nil).Shutdown))
}

// Start mocks base method.
func (m *MockRocketMQConsumer) Start() error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Start")
	ret0, _ := ret[0].(error)
	return ret0
}

// Start indicates an expected call of Start.
func (mr *MockRocketMQConsumerMockRecorder) Start() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Start", reflect.TypeOf((*MockRocketMQConsumer)(nil).Start))
}

// Subscribe mocks base method.
func (m *MockRocketMQConsumer) Subscribe(topic string, selector consumer.MessageSelector, f client.SubscribeFunc) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Subscribe", topic, selector, f)
	ret0, _ := ret[0].(error)
	return ret0
}

// Subscribe indicates an expected call of Subscribe.
func (mr *MockRocketMQConsumerMockRecorder) Subscribe(topic, selector, f interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Subscribe", reflect.TypeOf((*MockRocketMQConsumer)(nil).Subscribe), topic, selector, f)
}

// Suspend mocks base method.
func (m *MockRocketMQConsumer) Suspend() {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "Suspend")
}

// Suspend indicates an expected call of Suspend.
func (mr *MockRocketMQConsumerMockRecorder) Suspend() *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Suspend", reflect.TypeOf((*MockRocketMQConsumer)(nil).Suspend))
}

// Unsubscribe mocks base method.
func (m *MockRocketMQConsumer) Unsubscribe(topic string) error {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "Unsubscribe", topic)
	ret0, _ := ret[0].(error)
	return ret0
}

// Unsubscribe indicates an expected call of Unsubscribe.
func (mr *MockRocketMQConsumerMockRecorder) Unsubscribe(topic interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Unsubscribe", reflect.TypeOf((*MockRocketMQConsumer)(nil).Unsubscribe), topic)
}
